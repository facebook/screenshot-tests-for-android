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

import com.android.build.gradle.api.TestVariant

open class RunScreenshotTestTask : PullScreenshotsTask() {
  companion object {
    fun taskName(variant: TestVariant) = "run${variant.name.capitalize()}ScreenshotTest"
  }

  init {
    description = "Installs and runs screenshot tests, then generates a report"
    group = ScreenshotsPlugin.GROUP
  }

  override fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
    super.init(variant, extension)

    if (verify && extension.referenceDir != null) {
      return
    }

    dependsOn(variant.connectedInstrumentTest)
    mustRunAfter(variant.connectedInstrumentTest)
  }
}
