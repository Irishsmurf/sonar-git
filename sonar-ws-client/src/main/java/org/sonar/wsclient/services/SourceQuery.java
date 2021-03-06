/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.wsclient.services;

public class SourceQuery extends Query<Source> {
  public static final String BASE_URL = "/api/sources";

  private String resourceKeyOrId;
  private int from = 0;
  private int to = 0;
  private boolean highlightedSyntax = false;

  public SourceQuery(String resourceKeyOrId) {
    this.resourceKeyOrId = resourceKeyOrId;
  }

  public String getResourceKeyOrId() {
    return resourceKeyOrId;
  }

  public SourceQuery setResourceKeyOrId(String resourceKeyOrId) {
    this.resourceKeyOrId = resourceKeyOrId;
    return this;
  }

  public int getFrom() {
    return from;
  }

  /**
   * Get only a few lines
   *
   * @param from       Index of the first line, starts to 1
   * @param excludedTo Index of the last line (excluded).
   */
  public SourceQuery setFromLineToLine(int from, int excludedTo) {
    this.from = from;
    this.to = excludedTo;
    return this;
  }

  public SourceQuery setLinesFromLine(int from, int length) {
    this.from = from;
    this.to = from + length;
    return this;
  }

  public int getTo() {
    return to;
  }

  public boolean isHighlightedSyntax() {
    return highlightedSyntax;
  }

  public SourceQuery setHighlightedSyntax(boolean b) {
    this.highlightedSyntax = b;
    return this;
  }

  @Override
  public String getUrl() {
    StringBuilder url = new StringBuilder(BASE_URL);
    url.append("?resource=")
        .append(resourceKeyOrId)
        .append("&");
    if (from > 0 && to > 0) {
      url.append("from=").append(from).append("&to=").append(to).append("&");
    }
    if (highlightedSyntax) {
      url.append("color=true&");
    }
    return url.toString();
  }

  @Override
  public Class<Source> getModelClass() {
    return Source.class;
  }

  public static SourceQuery create(String resourceKeyOrId) {
    return new SourceQuery(resourceKeyOrId);
  }

  public static SourceQuery createWithHighlightedSyntax(String resourceKeyOrId) {
    return new SourceQuery(resourceKeyOrId).setHighlightedSyntax(true);
  }
}
