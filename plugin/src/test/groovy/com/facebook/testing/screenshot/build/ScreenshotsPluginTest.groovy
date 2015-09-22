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
  public void testApplies() {
    project.getPluginManager().apply 'com.android.library'
    project.getPluginManager().apply 'com.facebook.testing.screenshot'
  }

}
