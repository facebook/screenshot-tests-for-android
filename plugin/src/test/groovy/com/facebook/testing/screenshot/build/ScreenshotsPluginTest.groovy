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

  def setupProject() {
    // make an android manifest
    def mainDir = project.projectDir.toString() + "/src/main/"
    new File(mainDir).mkdirs()

    println("makig directories" + mainDir.toString())

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
      buildToolsVersion "23.0.1"
    }
  }

  @Test
  public void testHasTestDep() {
    project.getPluginManager().apply 'com.android.library'
    project.getPluginManager().apply 'com.facebook.testing.screenshot'
    setupProject()

    hasRuntimeDep()

    project.evaluate()
  }

  public void hasRuntimeDep() {
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
    hasRuntimeDep()
  }

  // @Test
  // public void testUsesTestApk() {
  //   def plugin = new ScreenshotsPlugin()
  //   project.getPluginManager().apply 'com.android.application'
  //   project.getPluginManager().apply 'com.facebook.testing.screenshot'

  //   assertEquals("foobar", plugin.getTestApkOutput(project))
  // }
}
