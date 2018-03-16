/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.build

import com.android.build.gradle.api.TestVariant


open class RecordScreenshotTestTask : RunScreenshotTestTask() {
  companion object {
    fun taskName(variant: TestVariant) = "record${variant.name.capitalize()}ScreenshotTest"
  }

  init {
    description = "Installs and runs screenshot tests, then records their output for later verification"
    group = ScreenshotsPlugin.GROUP
  }

  override fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
    super.init(variant, extension)
    record = true
  }
}