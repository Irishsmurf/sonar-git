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

package org.sonar.wsclient.unmarshallers;

import org.junit.Test;
import org.sonar.wsclient.JdkUtils;
import org.sonar.wsclient.services.Plugin;
import org.sonar.wsclient.services.WSUtils;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PluginUnmarshallerTest extends UnmarshallerTestCase {

  @Test
  public void toModel() throws Exception {
    WSUtils.setInstance(new JdkUtils());

    List<Plugin> plugins = new PluginUnmarshaller().toModels(loadFile("/plugins/plugins.json"));
    Plugin plugin = plugins.get(0);
    assertThat(plugin.getKey(), is("foo"));
    assertThat(plugin.getName(), is("Foo"));
    assertThat(plugin.getVersion(), is("1.0"));
  }

}
