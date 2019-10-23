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
import java.io.File


open class CleanScreenshotsTask : ScreenshotTask() {
  companion object {
    fun taskName(variant: TestVariant) = "clean${variant.name.capitalize()}Screenshots"
  }

  init {
    description = "Clean last generated screenshot report"
    group = ScreenshotsPlugin.GROUP
  }

  @TaskAction
  fun cleanScreenshots() {
    val outputDir = PullScreenshotsTask.getReportDir(project, variant)
    project.delete(outputDir)
  }
}
