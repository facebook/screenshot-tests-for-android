/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the license found in the LICENSE-examples file in the root
 * directory of this source tree.
 */

package com.facebook.screenshots

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.facebook.testing.screenshot.Screenshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule
  private var rule = ActivityTestRule(MainActivity::class.java, true, false)

  @Test
  fun openMainActivitySuccessfully() {
    val activity = rule.launchActivity(null)
    Screenshot.snapActivity(activity).record()
  }

}