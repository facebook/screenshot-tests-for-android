// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import android.support.test.runner.AndroidJUnitRunner;
import android.os.Bundle;

public class ScreenshotTestRunner extends AndroidJUnitRunner {
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
