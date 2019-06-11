/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.sample


import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.facebook.testing.screenshot.Screenshot
import org.hamcrest.Matcher

fun screenshot(name: String): ViewAction {
  return ScreenshotViewAction(name)
}

class ScreenshotViewAction internal constructor(private val name: String) : ViewAction {
  override fun getConstraints(): Matcher<View> {
    return isDisplayed()
  }

  override fun getDescription(): String {
    return name
  }

  override fun perform(uiController: UiController, view: View) {
    Screenshot.snap(view).setName(name).record()
  }
}
