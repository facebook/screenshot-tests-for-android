package com.facebook.testing.screenshot.build;

import org.junit.*
import static org.junit.Assert.*
import org.gradle.api.*
import org.gradle.testfixtures.*

class ScreenshotsPluginTest {
  def project;

  @Before
  public void setup() {
    project = ProjectBuilder.builder().build()
  }

  @Test
  public void testHasTestDep() {
    project.getPluginManager().apply 'com.android.library'
    project.getPluginManager().apply 'com.facebook.testing.screenshot'

    def depSet = project.getConfigurations().getByName('androidTestCompile').getAllDependencies()

    def found = false
    for (dep in depSet) {
      if (dep.name == "core" && dep.group == 'com.facebook.testing.screenshot') {
        found = true;
      }
    }

    assertTrue(found)
  }

  @Test
  public void testApplicationHappyPath() {
    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply 'com.facebook.testing.screenshot'
  }
}
