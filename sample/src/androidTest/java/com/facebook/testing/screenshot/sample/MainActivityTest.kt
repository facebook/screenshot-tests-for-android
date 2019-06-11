/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.sample


import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.facebook.testing.screenshot.Screenshot
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
  @get:Rule
  var activityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java, false, false)

  @Test
  fun testScreenshotEntireActivity() {
    val activity = activityTestRule.launchActivity(null)
    Screenshot.snapActivity(activity).record()
  }

  @Test
  fun mainActivityTestSettingsOpen() {
    val activity = activityTestRule.launchActivity(null)
    val floatingActionButton = onView(allOf<View>(withId(R.id.fab), isDisplayed()))
    floatingActionButton.perform(click())

    openActionBarOverflowOrOptionsMenu(activity)
    Screenshot.snapActivity(activity).record()
  }

  @Test
  fun mainActivityTestFabWithEspresso() {
    activityTestRule.launchActivity(null)
    onView(withId(R.id.fab)).perform(screenshot("fab"))
  }

  @Test
  fun errorTextShouldBeRed() {
    val intent = MainActivity.intent(MainActivity.Status.ERROR)
    val activity = activityTestRule.launchActivity(intent)

    Screenshot.snapActivity(activity).record()
  }

  @Test
  fun warningTextShouldBeYellow() {
    val intent = MainActivity.intent(MainActivity.Status.WARNING)
    val activity = activityTestRule.launchActivity(intent)

    Screenshot.snapActivity(activity).record()
  }

  @Test
  fun okTextShouldBeGreen() {
    val intent = MainActivity.intent(MainActivity.Status.OK)
    val activity = activityTestRule.launchActivity(intent)

    Screenshot.snapActivity(activity).record()
  }
}
