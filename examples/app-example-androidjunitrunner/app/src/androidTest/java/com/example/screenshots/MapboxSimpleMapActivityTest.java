package com.example.screenshots;

import android.support.test.rule.ActivityTestRule;

import com.facebook.testing.screenshot.Screenshot;

import org.junit.Rule;
import org.junit.Test;


public class MapboxSimpleMapActivityTest {

  @Rule
  public ActivityTestRule<MapboxSimpleMapActivity> mActivityTestRule =
    new ActivityTestRule<>(MapboxSimpleMapActivity.class);

  @Test
  public void checksMapboxSimpleMap() {
    // Wait 5 seconds until the map gets completely loaded
    // TODO Use a custom idling resource to avoid sleep
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Screenshot.snapActivity(mActivityTestRule.getActivity()).record();
  }
}
