package com.example.screenshots;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import com.facebook.testing.screenshot.Screenshot;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

public final class ScreenshotViewAction implements ViewAction {
    public static ViewAction screenshot(String name) {
        return new ScreenshotViewAction(name);
    }

    private final String name;

    ScreenshotViewAction(final String name) {
        this.name = name;
    }

    @Override
    public Matcher<View> getConstraints() {
        return isDisplayed();
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public void perform(final UiController uiController, final View view) {
        Screenshot.snap(view).setName(name).record();
    }
}
