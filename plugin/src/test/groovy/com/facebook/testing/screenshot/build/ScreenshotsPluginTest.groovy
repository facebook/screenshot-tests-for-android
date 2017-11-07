package com.facebook.testing.screenshot.build;

import org.junit.*
import static org.junit.Assert.*
import org.gradle.api.*
import org.gradle.testfixtures.*
import static org.hamcrest.CoreMatchers.*

class ScreenshotsPluginForTest extends ScreenshotsPlugin {
  static public runtimeDepAdded = false

  @Override
  void addRuntimeDep(Project project) {
    runtimeDepAdded = true
  }
}

class ScreenshotsPluginTest {
  Project project

  @Before
  void setup() {
    project = ProjectBuilder.builder().build()
  }

  @After
  void tearDown() {
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
  void testHasTestDep() {
    project.getPluginManager().apply 'com.android.library'
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()

    assertTrue(ScreenshotsPluginForTest.runtimeDepAdded)
    project.evaluate()
  }

  static void hasRuntimeDep(Project project) {
    def depSet = project.getConfigurations().getByName('androidTestApi').getAllDependencies()

    def found = false
    for (dep in depSet) {
      if (dep.name == "core" && dep.group == 'com.facebook.testing.screenshot') {
        found = true
      }
    }

    assertTrue(found)
  }

  @Test
  void testApplicationHappyPath() {
    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()

    project.evaluate()
  }

  @Test
  void testUsesTestApk() {
    def plugin = new ScreenshotsPlugin()
    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()
    project.evaluate()

    // Create dummy APK file to find
    Task task = project.tasks.getByPath(project.screenshots.testApkTarget)
    for (File dir in task.outputs.files) {
      if (dir.absolutePath.contains("outputs/apk/androidTest")) {
        assertTrue(dir.mkdirs())
        File dummyAPK = new File(dir, "test.apk")
        assertTrue(dummyAPK.createNewFile())
        break
      }
    }

    assert plugin.getTestApkOutput(project).contains("androidTest")
  }

  @Test
  void testCanSetApkTarget() {
    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()
    project.screenshots.testApkTarget = "packageReleaseAndroidTest"

    project.evaluate()
    def deps = project.tasks.getByPath("pullScreenshots").getDependsOn()

    assert deps.contains("packageReleaseAndroidTest")
  }

  @Test
  void testAddRuntimeDep() {
    project.getPluginManager().apply 'com.android.application'

    def plugin = new ScreenshotsPlugin()
    plugin.addRuntimeDep(project)

    hasRuntimeDep(project)
  }

  @Test
  void addsLocalScreenshotsTask() {
    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply ScreenshotsPluginForTest
    setupProject()

    assertTrue(project.tasks.pullScreenshotsFromDirectory instanceof Task)
  }
}
