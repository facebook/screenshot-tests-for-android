/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

      defaultConfig {
        applicationId appId
      }
    }
  }

  @Test
  void "Ensure core dependency added"() {
    project.evaluate()

    def depSet = project.getConfigurations().getByName('androidTestImplementation').getAllDependencies()
    for (dep in depSet) {
      if (dep.name == "core" && dep.group == 'com.facebook.testing.screenshot') {
        return
      }
    }
    fail()
  }

  @Test
  void "Ensure core dependency not added when requested"() {
    project.screenshots {
      addDeps = false
    }
    project.evaluate()

    def depSet = project.getConfigurations().getByName('androidTestImplementation').getAllDependencies()
    for (dep in depSet) {
      if (dep.name == "core" && dep.group == 'com.facebook.testing.screenshot') {
        fail()
      }
    }
  }

  @Test
  void "Ensure tasks added"() {
    project.evaluate()

    assertTrue(project.tasks.pullDebugAndroidTestScreenshots instanceof Task)
    assertTrue(project.tasks.runDebugAndroidTestScreenshotTest instanceof Task)
    assertTrue(project.tasks.recordDebugAndroidTestScreenshotTest instanceof Task)
    assertTrue(project.tasks.verifyDebugAndroidTestScreenshotTest instanceof Task)
  }
}
