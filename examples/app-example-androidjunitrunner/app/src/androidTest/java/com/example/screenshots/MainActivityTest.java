package com.example.screenshots;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;

import com.facebook.testing.screenshot.Screenshot;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.example.screenshots.ScreenshotViewAction.screenshot;
import static org.hamcrest.Matchers.allOf;


public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() {
        Screenshot.snapActivity(mActivityTestRule.getActivity()).record();
    }

    @Test
    public void mainActivityTestSettingsOpen() {
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab), isDisplayed()));
        floatingActionButton.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        Screenshot.snapActivity(mActivityTestRule.getActivity()).record();
    }

    @Test
    public void mainActivityTestFabWithEspresso() {
        onView(withId(R.id.fab)).perform(screenshot("fab"));
    }
}
