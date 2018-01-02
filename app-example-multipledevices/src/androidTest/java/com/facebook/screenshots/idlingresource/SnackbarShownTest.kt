/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the license found in the LICENSE-examples file in the root
 * directory of this source tree.
 */

package com.facebook.screenshots.idlingresource

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import com.facebook.screenshots.MainActivity
import com.facebook.screenshots.R
import com.facebook.testing.screenshot.Screenshot
import org.junit.After
import org.junit.Rule
import org.junit.Test

class SnackbarShownTest {

  @get:Rule
  private var rule = ActivityTestRule(MainActivity::class.java, true, false)

  @Test
  fun clickOnFab_showSnackbar() {
    val activity = rule.launchActivity(null)
    val snackbarIdlingResource = SnackbarTextIdlingResource(activity, R.string.hi_text)

    onView(withId(R.id.fab)).perform(click())

    IdlingRegistry.getInstance().register(snackbarIdlingResource)

    Screenshot.snapActivity(activity).record()
  }

  @After
  fun tearDown() {
    IdlingRegistry.getInstance().resources.forEach { IdlingRegistry.getInstance().unregister(it) }
  }
}