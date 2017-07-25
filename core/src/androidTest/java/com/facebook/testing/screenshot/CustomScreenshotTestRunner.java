// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

public class CustomScreenshotTestRunner extends AndroidJUnitRunner {
  @Override
  public void onCreate(Bundle args) {
    ScreenshotRunner.onCreate(this, args);
    super.onCreate(args);
  }

  @Override
  public void finish(int resultCode, Bundle results) {
    ScreenshotRunner.onDestroy();
    super.finish(resultCode, results);
  }
}
