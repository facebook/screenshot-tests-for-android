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
