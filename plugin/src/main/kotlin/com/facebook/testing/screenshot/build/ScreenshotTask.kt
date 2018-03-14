/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.build

import com.android.build.gradle.api.TestVariant
import org.gradle.api.DefaultTask

open class ScreenshotTask : DefaultTask() {
  protected lateinit var extension: ScreenshotsPluginExtension

  open fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
    this.extension = extension
  }
}
