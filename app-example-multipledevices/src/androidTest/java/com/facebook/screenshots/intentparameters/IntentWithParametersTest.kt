/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the license found in the LICENSE-examples file in the root
 * directory of this source tree.
 */

package com.facebook.screenshots.intentparameters

import android.content.Intent
import android.support.test.rule.ActivityTestRule
import com.facebook.screenshots.MESSAGE_TYPE_KEY
import com.facebook.screenshots.MainActivity
import com.facebook.screenshots.MessageType
import com.facebook.testing.screenshot.Screenshot
import org.junit.Rule
import org.junit.Test

class IntentWithParametersTest {

  @get:Rule
  private var rule = ActivityTestRule(MainActivity::class.java, true, false)

  @Test
  fun warningTextColorShown() {
    val activity = rule.launchActivity(intent(MessageType.WARNING))
    Screenshot.snapActivity(activity).record()
  }

  @Test
  fun successTextColorShown() {
    val activity = rule.launchActivity(intent(MessageType.SUCCESS))
    Screenshot.snapActivity(activity).record()
  }

  @Test
  fun errorTextColorShown() {
    val activity = rule.launchActivity(intent(MessageType.ERROR))
    Screenshot.snapActivity(activity).record()
  }

  private fun intent(messageType: Int) = Intent().apply {
    putExtra(MESSAGE_TYPE_KEY, messageType)
  }
}