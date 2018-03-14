/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.build

import com.facebook.testing.screenshot.generated.ScreenshotTestBuildConfig
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.TestVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

open class ScreenshotsPluginExtension {
  /** The directory to store recorded screenshots in */
  var recordDir = "screenshots"
  /** Whether to have the plugin dependency automatically add the core dependency */
  var addDeps = true
  /** Whether to store screenshots in device specific folders */
  var multipleDevices = true
  /** The python executable to use */
  var pythonExecutable = "python"
}

class ScreenshotsPlugin : Plugin<Project> {
  companion object {
    const val GROUP = "Screenshot Test"
    const val DEPENDENCY_GROUP = "com.facebook.testing.screenshot"
    const val DEPENDENCY_CORE = "core"
  }

  private lateinit var screenshotExtensions: ScreenshotsPluginExtension

  override fun apply(project: Project) {
    val extensions = project.extensions
    val plugins = project.plugins
    screenshotExtensions = extensions.create("screenshots", ScreenshotsPluginExtension::class.java)

    if (screenshotExtensions.addDeps) {
      project.dependencies.add("androidTestApi", "$DEPENDENCY_GROUP:$DEPENDENCY_CORE:${ScreenshotTestBuildConfig.VERSION}")
    }

    val variants = when {
      plugins.hasPlugin("com.android.application") ->
        extensions.findByType(AppExtension::class.java)!!.testVariants
      plugins.hasPlugin("com.android.library") ->
        extensions.findByType(LibraryExtension::class.java)!!.testVariants
      else -> throw IllegalArgumentException("Screenshot Test plugin requires Android's plugin")
    }

    variants.all { generateTasksFor(project, it) }
  }

  private fun <T : ScreenshotTask> createTask(
      project: Project, name: String, variant: TestVariant, clazz: Class<T>): T {
    return project.tasks.create(name, clazz).apply { init(variant, screenshotExtensions) }
  }

  private fun generateTasksFor(project: Project, variant: TestVariant) {
    variant.outputs.all {
      if (it is ApkVariantOutput) {
        createTask(
            project,
            PullScreenshotsTask.taskName(variant),
            variant,
            PullScreenshotsTask::class.java)

        createTask(
            project,
            RunScreenshotTestTask.taskName(variant),
            variant,
            RunScreenshotTestTask::class.java)

        createTask(
            project,
            RecordScreenshotTestTask.taskName(variant),
            variant,
            RecordScreenshotTestTask::class.java)

        createTask(
            project,
            VerifyScreenshotTestTask.taskName(variant),
            variant,
            VerifyScreenshotTestTask::class.java)
      }
    }
  }
}