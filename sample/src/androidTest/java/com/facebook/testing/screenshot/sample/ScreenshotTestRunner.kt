/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.sample

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.facebook.litho.config.ComponentsConfiguration
import com.facebook.testing.screenshot.ScreenshotRunner
import com.facebook.testing.screenshot.layouthierarchy.LayoutHierarchyDumper
import com.facebook.testing.screenshot.layouthierarchy.litho.LithoAttributePlugin
import com.facebook.testing.screenshot.layouthierarchy.litho.LithoHierarchyPlugin

class ScreenshotTestRunner : AndroidJUnitRunner() {
  companion object {
    init {
      ComponentsConfiguration.isDebugModeEnabled = true
      LayoutHierarchyDumper.addGlobalHierarchyPlugin(LithoHierarchyPlugin.getInstance())
      LayoutHierarchyDumper.addGlobalAttributePlugin(LithoAttributePlugin.getInstance())
    }
  }

  override fun onCreate(arguments: Bundle) {
    ScreenshotRunner.onCreate(this, arguments)
    super.onCreate(arguments)
  }

  override fun finish(resultCode: Int, results: Bundle) {
    ScreenshotRunner.onDestroy()
    super.finish(resultCode, results)
  }
}

