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

