/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.build

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

class ScreenshotsPluginTest {
  Project project

  @Before
  void "setup"() {
    final appId = "com.facebook.testing.screenshot.integration"
    project = ProjectBuilder.builder().build()

    File manifest = new File(project.projectDir, "src/main/AndroidManifest.xml")
    manifest.parentFile.mkdirs()
    manifest.write("""<?xml version="1.0" encoding="utf-8"?>
      <manifest package="$appId">
        <application/>
      </manifest>""")

    project.getPluginManager().apply 'com.android.application'
    project.getPluginManager().apply ScreenshotsPlugin

    project.repositories {
      mavenCentral()
    }

    project.android {
      compileSdkVersion 22
      buildToolsVersion "26.0.1"

      defaultConfig {
        applicationId appId
      }
    }

    project.evaluate()
  }

  @Test
  void "Ensure core dependency added"() {
    def depSet = project.getConfigurations().getByName('androidTestApi').getAllDependencies()
    for (dep in depSet) {
      if (dep.name == "core" && dep.group == 'com.facebook.testing.screenshot') {
        return
      }
    }
    fail()
  }

  @Test
  void "Ensure tasks added"() {
    assertTrue(project.tasks.pullDebugAndroidTestScreenshots instanceof Task)
    assertTrue(project.tasks.runDebugAndroidTestScreenshotTest instanceof Task)
    assertTrue(project.tasks.recordDebugAndroidTestScreenshotTest instanceof Task)
    assertTrue(project.tasks.verifyDebugAndroidTestScreenshotTest instanceof Task)
  }
}
