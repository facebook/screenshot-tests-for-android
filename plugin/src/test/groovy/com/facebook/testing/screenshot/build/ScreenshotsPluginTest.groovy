package com.facebook.testing.screenshot.build;

import org.junit.*
import static org.junit.Assert.*
import org.gradle.api.*
import org.gradle.testfixtures.*
import static org.hamcrest.Matchers.*

class ScreenshotsPluginForTest extends ScreenshotsPlugin {
  static public runtimeDepAdded = false

  @Override
  void addRuntimeDep(Project project) {
    runtimeDepAdded = true
  }
}

class ScreenshotsPluginTest {
  def project;

  @Before
  public void setup() {
    project = ProjectBuilder.builder().build()
  }

  @After
  public void tearDown() {
    ScreenshotsPluginForTest.runtimeDepAdded = false
  }

  def setupProject() {
    // make an android manifest
    def mainDir = project.projectDir.toString() + "/src/main/"
    new File(mainDir).mkdirs()

    println("making directories" + mainDir.toString())

    def manifest = new File(mainDir + "/AndroidManifest.xml")

    manifest.withWriter('utf-8') { writer ->
      writer.writeLine('''<?xml version="1.0" encoding="utf-8"?>
<manifest
    xmlns:android="http://schemas.android.com/apk/res/android"
    package='com.facebook.testing.screenshot.examples'>
   <uses-sdk android:minSdkVersion='9' android:targetSdkVersion="22" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   <application>
     <!-- not having an empty application block seems to cause the
          instrumentation tests to fail with a ClassNotFoundException . -->
   </application>
</manifest>''')
    }

    project.repositories {
      mavenCentral()
    }


    project.android {
      compileSdkVersion 22
      buildToolsVersion "26.0.1"
    }
  }

  @Test
  public void testHasTestDep() {
    project.getPluginManager().apply 'com.android.library'
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()

    assertTrue(ScreenshotsPluginForTest.runtimeDepAdded)
    project.evaluate()
  }

  public void hasRuntimeDep(Project project) {
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
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()

    project.evaluate()
  }

  @Test
  public void testUsesTestApk() {
    def plugin = new ScreenshotsPlugin()
    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()
    project.evaluate()

    assert plugin.getTestApkOutput(project).contains("androidTest")
  }

  @Test
  public void testCanSetApkTarget() {
    def plugin = new ScreenshotsPlugin()
    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()
    project.screenshots.testApkTarget = "packageReleaseAndroidTest"

    project.evaluate()
    def deps = project.tasks.getByPath("pullScreenshots").getDependsOn()

    assert deps.contains("packageReleaseAndroidTest")
  }

  @Test
  public void testAddRuntimeDep() {
    project.getPluginManager().apply 'com.android.application'

    def plugin = new ScreenshotsPlugin()
    plugin.addRuntimeDep(project)

    hasRuntimeDep(project);
  }

  @Test
  public void testUsingAdbConfigurationThrowsError() {
    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply ScreenshotsPluginForTest

    try {
      project.screenshots.adb = "foobar"
      fail("Expected exception")
    } catch (IllegalArgumentException cause) {
      assertThat(cause.getMessage(), containsString("deprecated"));
    }
  }

  @Test
  public void addsLocalScreenshotsTask() {
    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()

    assertTrue(project.tasks.pullScreenshotsFromDirectory instanceof Task)
  }
}
