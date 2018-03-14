/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.build

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.TestVariant
import org.gradle.api.tasks.TaskAction
import java.io.File


open class PullScreenshotsTask : ScreenshotTask() {
  companion object {
    fun taskName(variant: TestVariant) = "pull${variant.name.capitalize()}Screenshots"
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
    apkPath = variant.outputs.find { it is ApkVariantOutput }!!.outputFile
  }

  @TaskAction
  fun pullScreenshots() {
    val codeSource = ScreenshotsPlugin::class.java.protectionDomain.codeSource
    val jarFile = File(codeSource.location.toURI().path)

    project.exec {
      it.executable = "python"
      it.environment("PYTHONPATH", jarFile)

      it.args = mutableListOf(
        "-m",
        "android_screenshot_tests.pull_screenshots",
        "--apk",
        apkPath.absolutePath
      ).apply {
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