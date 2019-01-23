/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

  private var referenceDir: String? = null
  private var deviceName: String? = null

  protected var verify = false
  protected var record = false

  init {
    description = "Pull screenshots from your device"
    group = ScreenshotsPlugin.GROUP
  }

  override fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
    super.init(variant, extension)
    referenceDir = extension.referenceDir
    deviceName = extension.deviceName
    
    if (referenceDir == null) {
      apkPath = variant.outputs.find { it is ApkVariantOutput }!!.outputFile
    }
  }

  @TaskAction
  fun pullScreenshots() {
    val codeSource = ScreenshotsPlugin::class.java.protectionDomain.codeSource
    val jarFile = File(codeSource.location.toURI().path)
    val outputDir = getReportDir(project, variant)

    assert(!outputDir.exists())

    project.exec {
      it.executable = "python"
      it.environment("PYTHONPATH", jarFile)

      val noPull = referenceDir != null

      val tempDir = if (noPull) {
        referenceDir
      } else {
        outputDir.absolutePath
      }

      it.args = mutableListOf(
        "-m",
        "android_screenshot_tests.pull_screenshots",
        "--temp-dir",
        tempDir
      ).apply {
        if (noPull) {
          add("--no-pull")
        } else {
          add("--apk")
          add(apkPath.absolutePath)
        }

        if (!deviceName.isNullOrEmpty()) {
          add("--device-name")
          add(deviceName)
        }

        if (verify) {
          add("--verify")
        } else if (record) {
          add("--record")
        }

        if (verify || record) {
          add(extension.recordDir)
        }

        if (extension.multipleDevices) {
          add("--multiple-devices")
          add("${extension.multipleDevices}")
        }
      }

      println(it.args)
    }
  }
}
