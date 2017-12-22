/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the license found in the LICENSE-examples file in the root
 * directory of this source tree.
 */
package com.example.screenshots;

import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.test.runner.AndroidJUnitRunner;
import com.facebook.testing.screenshot.ScreenshotRunner;

public class CustomTestRunner extends AndroidJUnitRunner {

  @Override
  public void onCreate(Bundle arguments) {
    MultiDex.install(getTargetContext());
    ScreenshotRunner.onCreate(this, arguments);
    super.onCreate(arguments);
  }

  @Override
  public void finish(int resultCode, Bundle results) {
    ScreenshotRunner.onDestroy();
    super.finish(resultCode, results);
  }
}
