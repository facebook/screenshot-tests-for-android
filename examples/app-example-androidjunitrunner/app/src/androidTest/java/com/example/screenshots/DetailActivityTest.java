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
    Activity activity = startActivity(givenAnErrorText());

    Screenshot.snapActivity(activity).record();
  }

  @Test
  public void warningTextShouldBeYellow() {
    Activity activity = startActivity(givenAWarningText());

    Screenshot.snapActivity(activity).record();
  }

  @Test
  public void okTextShouldBeGreen() {
    Activity activity = startActivity(givenAnOkText());

    Screenshot.snapActivity(activity).record();
  }

  private Intent givenAnErrorText() {
    Intent intent = new Intent();
    intent.putExtra(DetailActivity.TYPE,
        DetailActivity.Type.ERROR);
    intent.putExtra(DetailActivity.TEXT,
        "Error 500: internal server error");

    return intent;
  }

  private Intent givenAWarningText() {
    Intent intent = new Intent();
    intent.putExtra(DetailActivity.TYPE,
        DetailActivity.Type.WARNING);
    intent.putExtra(DetailActivity.TEXT,
        "Method onAttach(Context context) is deprecated");

    return intent;
  }

  private Intent givenAnOkText() {
    Intent intent = new Intent();
    intent.putExtra(DetailActivity.TYPE,
        DetailActivity.Type.OK);
    intent.putExtra(DetailActivity.TEXT,
        "Screenshot testing is wonderful");

    return intent;
  }

  private Activity startActivity(Intent intent) {
    return mActivityTestRule.launchActivity(intent);
  }
}