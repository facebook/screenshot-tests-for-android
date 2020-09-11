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

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.TestVariant
import org.gradle.api.tasks.TaskAction
import org.gradle.api.Project
import java.io.File


open class PullScreenshotsTask : ScreenshotTask() {
  companion object {
      fun taskName(variant: TestVariant) = "pull${variant.name.capitalize()}Screenshots"

      fun getReportDir(project: Project, variant: TestVariant): File =
          File(project.buildDir, "screenshots" + variant.name.capitalize())
  }

  private lateinit var apkPath: File
  protected var verify = false
  protected var record = false

  init {
    description = "Pull screenshots from your device"
    group = ScreenshotsPlugin.GROUP
  }

  override fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
    super.init(variant, extension)
    val output = variant.outputs.find { it is ApkVariantOutput } as? ApkVariantOutput
        ?: throw IllegalArgumentException("Can't find APK output")
    val packageTask = variant.packageApplicationProvider.orNull
        ?: throw IllegalArgumentException("Can't find package application provider")
    
    apkPath = File(packageTask.outputDirectory.asFile.get(), output.outputFileName)
  }

  @TaskAction
  fun pullScreenshots() {
    val codeSource = ScreenshotsPlugin::class.java.protectionDomain.codeSource
    val jarFile = File(codeSource.location.toURI().path)
    val isVerifyOnly = verify && extension.referenceDir != null

    val outputDir = if (isVerifyOnly) {
      File(extension.referenceDir)
    } else {
      getReportDir(project, variant)
    }

    assert(if (isVerifyOnly) outputDir.exists() else !outputDir.exists())

    project.exec {
      it.executable = extension.pythonExecutable
      it.environment("PYTHONPATH", jarFile)

      it.args = mutableListOf(
        "-m",
        "android_screenshot_tests.pull_screenshots",
        "--apk",
        apkPath.absolutePath,
        "--temp-dir",
        outputDir.absolutePath
      ).apply {
        if (verify) {
          add("--verify")
        } else if (record) {
          add("--record")
        }

        if (verify || record) {
          add(extension.recordDir)
        }

        if (verify && extension.failureDir != null) {
            add("--failure-dir")
            add("${extension.failureDir}")
        }

        if (extension.multipleDevices) {
          add("--multiple-devices")
          add("${extension.multipleDevices}")
        }

        if (isVerifyOnly) {
          add("--no-pull")
        }
      }

      println(it.args)
    }
  }
}
