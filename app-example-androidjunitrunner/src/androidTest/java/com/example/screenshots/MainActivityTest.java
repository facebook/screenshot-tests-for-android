/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the license found in the LICENSE-examples file in the root
 * directory of this source tree.
 */
package com.example.screenshots;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.example.screenshots.ScreenshotViewAction.screenshot;
import static org.hamcrest.Matchers.allOf;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import com.facebook.testing.screenshot.Screenshot;
import org.junit.Rule;
import org.junit.Test;

public class MainActivityTest {

  @Rule
  public ActivityTestRule<MainActivity> mActivityTestRule =
      new ActivityTestRule<>(MainActivity.class, true, false);

  @Test
  public void mainActivityTest() {
    final MainActivity activity = mActivityTestRule.launchActivity(null);

    Screenshot.snapActivity(activity).record();
  }

  @Test
  public void mainActivityTestSettingsOpen() {
    final MainActivity activity = mActivityTestRule.launchActivity(null);
    ViewInteraction floatingActionButton = onView(allOf(withId(R.id.fab), isDisplayed()));
    floatingActionButton.perform(click());

    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    Screenshot.snapActivity(activity).record();
  }

  @Test
  public void mainActivityTestFabWithEspresso() {
    mActivityTestRule.launchActivity(null);
    onView(withId(R.id.fab)).perform(screenshot("fab"));
  }
}
