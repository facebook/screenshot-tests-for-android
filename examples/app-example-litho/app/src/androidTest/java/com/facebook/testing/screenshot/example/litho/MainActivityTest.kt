/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.example.litho

import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.facebook.litho.config.ComponentsConfiguration
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ScreenshotRunner
import com.facebook.testing.screenshot.layouthierarchy.LayoutHierarchyDumper
import com.facebook.testing.screenshot.layouthierarchy.litho.LithoAttributePlugin
import com.facebook.testing.screenshot.layouthierarchy.litho.LithoHierarchyPlugin
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
  companion object {
    init {
      ComponentsConfiguration.isDebugModeEnabled = true
      LayoutHierarchyDumper.addGlobalHierarchyPlugin(LithoHierarchyPlugin.getInstance())
      LayoutHierarchyDumper.addGlobalAttributePlugin(LithoAttributePlugin.getInstance())
    }
  }

  @get:Rule
  var activityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

  @Before
  fun before() {
    ScreenshotRunner.onCreate(
        InstrumentationRegistry.getInstrumentation(), InstrumentationRegistry.getArguments())
  }

  @After
  fun after() {
    ScreenshotRunner.onDestroy()
  }

  @Test
  fun testScreenshotEntireActivity() {
    Screenshot.snapActivity(activityTestRule.activity).record()
  }
}
