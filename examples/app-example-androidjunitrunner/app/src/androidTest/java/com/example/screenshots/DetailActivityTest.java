// Copyright 2004-present Facebook. All Rights Reserved.

package com.example.screenshots;

import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import com.facebook.testing.screenshot.Screenshot;
import org.junit.Rule;
import org.junit.Test;

public class DetailActivityTest {

  @Rule
  public ActivityTestRule<DetailActivity> mActivityTestRule =
      new ActivityTestRule<>(DetailActivity.class, true, false);

  @Test
  public void errorTextShouldBeRed() {
    final Intent intent = given(DetailActivity.Type.ERROR, "Error 500: internal server error");
    Activity activity = startActivity(intent);

    Screenshot.snapActivity(activity).record();
  }

  @Test
  public void warningTextShouldBeYellow() {
    final Intent intent =
        given(DetailActivity.Type.WARNING, "Method onAttach(Context context) is deprecated");
    Activity activity = startActivity(intent);

    Screenshot.snapActivity(activity).record();
  }

  @Test
  public void okTextShouldBeGreen() {
    final Intent intent = given(DetailActivity.Type.OK, "Screenshot testing is wonderful");
    Activity activity = startActivity(intent);

    Screenshot.snapActivity(activity).record();
  }

  private Intent given(DetailActivity.Type type, String text) {
    Intent intent = new Intent();
    intent.putExtra(DetailActivity.TYPE, type);
    intent.putExtra(DetailActivity.TEXT, text);

    return intent;
  }

  private Activity startActivity(Intent intent) {
    return mActivityTestRule.launchActivity(intent);
  }
}
