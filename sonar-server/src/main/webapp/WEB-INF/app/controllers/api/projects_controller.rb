#
# Sonar, entreprise quality control tool.
# Copyright (C) 2008-2011 SonarSource
# mailto:contact AT sonarsource DOT com
#
# Sonar is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# Sonar is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with Sonar; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
#
class Api::ProjectsController < Api::ApiController
  
  # PARAMETERS
  #   subprojects [true|false] : load sub-projects ? Default is false. Ignored if the parameter key is set.
  #   views [true|false] : load views and sub-views ? Default is false. Ignored if the parameter key is set.
  #   libs [true|false] : load libraries ? Default is false. Ignored if the parameter key is set.
  #   langs (comma-separated list) : filter results by language. Default is empty.
  #   desc [true|false] : load project description ? Default is false.
  #   key : id or key of the project (0 or 1 result)
  #   search : substring of project name, case insensitive  
  #   versions [true,false,last]. Default is false.
  
  # TODO
  # - SQL pagination (LIMIT + OFFSET)
    
  def index
    begin
      @show_description=(params[:desc]=='true')
      @projects=load_projects
      @snapshots_by_pid=load_snapshots_by_project
      respond_to do |format| 
        format.json{ render :json => jsonp(to_json) }
        format.xml { render :xml => to_xml }
        format.text { render :text => text_not_supported }
      end
      
    rescue Exception => e
      logger.error("Fails to execute #{request.url} : #{e.message}")
      render_error(e.message)
    end
  end
  
  
  
  private
  
  def load_projects
    conditions=['enabled=:enabled']
    values={:enabled => true}
    
    if !params[:langs].blank?
      conditions<<'language in (:langs)'
      values[:langs]=params[:langs].split(',')
    end
  
    if !params[:key].blank?
      begin
        id=Integer(params[:key])
        conditions<<'id=:id'
        values[:id]=id
      rescue
        conditions<<'kee=:key'
        values[:key]=params[:key]
      end
    else 
      if !params[:search].blank?
        conditions<<"UPPER(name) like :name"
        values[:name]='%' + params[:search].upcase + '%'
      end
      
      scopes=['PRJ']
      qualifiers=['TRK']
      if params[:views]=='true'
        qualifiers<<'VW'<<'SVW'
      end
      if params[:subprojects]=='true'
        qualifiers<<'BRC'
      end
      if params[:libs]=='true'
        qualifiers<<'LIB'
      end
      conditions<<'scope in (:scopes) AND qualifier in (:qualifiers)'
      values[:scopes]=scopes
      values[:qualifiers]=qualifiers
    end
  
    # this is really an advanced optimization !
    select_columns='id,kee,name,language,long_name,scope,qualifier,root_id'
    select_columns += ',description' if @show_description
      
    projects=Project.find(:all, :select => select_columns, :conditions => [conditions.join(' AND '), values], :order => 'name')
    select_authorized(:user, projects)
  end
  
  def load_snapshots_by_project
    select_columns='id,project_id,version,islast,created_at'
    if params[:versions]=='true'
      snapshots=Snapshot.find_by_sql(["SELECT #{select_columns} FROM snapshots AS s1 WHERE s1.status=? AND s1.project_id IN (?) AND NOT EXISTS(SELECT * FROM snapshots AS s2 WHERE s2.project_id=s1.project_id AND s2.created_at>s1.created_at AND s2.version=s1.version)", 'P', @projects.map{|p| p.id}])
    elsif params[:versions]=='last'
      snapshots=Snapshot.find_by_sql(["SELECT #{select_columns} FROM snapshots AS s1 WHERE s1.status=? AND islast=? AND s1.project_id IN (?) AND NOT EXISTS(SELECT * FROM snapshots AS s2 WHERE s2.project_id=s1.project_id AND s2.created_at>s1.created_at AND s2.version=s1.version)", 'P', true, @projects.map{|p| p.id}])
    else
      snapshots=[]
    end
    
    snapshots_by_project_id={}
    snapshots.each do |s|
      snapshots_by_project_id[s.project_id]||=[]
      snapshots_by_project_id[s.project_id]<<s
    end
    snapshots_by_project_id
  end

  def to_json
    json=[]
    @projects.each do |project|
      hash={}
      hash[:id]=project.id.to_s
      hash[:k]=project.key
      hash[:nm]=project.name(true)
      hash[:sc]=project.scope
      hash[:qu]=project.qualifier
      hash[:ds]=project.description if @show_description && project.description
      
      if @snapshots_by_pid && @snapshots_by_pid[project.id]
        versions={}
        @snapshots_by_pid[project.id].sort{|s1,s2| s2.version <=> s1.version}.each do |snapshot|
          version={:sid => snapshot.id.to_s}
          version[:d]=format_datetime(snapshot.created_at) if snapshot.created_at
          if snapshot.last?
            hash[:lv]=snapshot.version
          end
          versions[snapshot.version]=version
        end
        hash[:v]=versions
      end
      json<<hash
    end
    json
  end
  
  def to_xml
    xml = Builder::XmlMarkup.new(:indent => 0)
    xml.projects do 
      @projects.each do |project|
        xml.project(:id => project.id.to_s, :key => project.key) do 
          xml.name(project.name(true))
          xml.scope(project.scope)
          xml.qualifier(project.qualifier)
          xml.desc(project.description) if @show_description && project.description
          
          if @snapshots_by_pid && @snapshots_by_pid[project.id]
            @snapshots_by_pid[project.id].sort{|s1,s2| s2.version <=> s1.version}.each do |snapshot|
              attributes={:sid => snapshot.id.to_s, :last => snapshot.last?}
              attributes[:date]=format_datetime(snapshot.created_at) if snapshot.created_at
              xml.version(snapshot.version, attributes)
            end
          end
        end
      end
    end
  end

end