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
package org.sonar.api.checks.profiles;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.sonar.check.Priority;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

/**
 * Created by IntelliJ IDEA.
 * User: simonbrandhof
 * Date: Sep 14, 2010
 * Time: 11:02:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationCheckProfileFactoryTest {

  @Test
  public void noChecks() {
    assertThat(AnnotationCheckProfileFactory.create("plugin_foo", "java", null).size(), is(0));
    assertThat(AnnotationCheckProfileFactory.create("plugin_foo", "java", Collections.<Class>emptyList()).size(), is(0));
  }

  @Test
  public void create() {
    Collection<CheckProfile> profiles = AnnotationCheckProfileFactory.create("plugin_foo", "java", Arrays.<Class>asList(FakeCheckOne.class));
    assertThat(profiles.size(), is(1));

    CheckProfile profile = profiles.iterator().next();
    assertThat(profile.getName(), is("profile one"));
    assertThat(profile.getChecks().size(), is(1));
    assertThat(profile.getChecks().get(0).getPriority(), is(Priority.MINOR));
  }

  @Test
  public void provideManyProfiles() {
    Collection<CheckProfile> profiles = AnnotationCheckProfileFactory.create("plugin_foo", "java", Arrays.<Class>asList(FakeCheckOne.class, FakeCheckTwo.class));
    assertThat(profiles.size(), is(2));

    assertThat(profiles, hasItem(new CheckProfileMatcher("profile one", 2)));
    assertThat(profiles, hasItem(new CheckProfileMatcher("profile two", 1)));
  }
}

class CheckProfileMatcher extends BaseMatcher<CheckProfile> {
  private String name;
  private int numberOfChecks;

  CheckProfileMatcher(String name, int numberOfChecks) {
    this.name = name;
    this.numberOfChecks = numberOfChecks;
  }

  public boolean matches(Object o) {
    CheckProfile profile = (CheckProfile) o;
    return profile.getName().equals(name) && profile.getChecks().size() == numberOfChecks;
  }

  public void describeTo(Description description) {
  }
}