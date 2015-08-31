/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

import android.app.Activity;
import android.view.View;

import com.facebook.testing.screenshot.internal.ScreenshotImpl;

/**
 * A testing tool for taking a screenshot during an Activity
 * instrumentation test. This is really useful while manually
 * investigating how the rendering looks like after setting up some
 * complex set of conditions in the test. (Which might be hard to
 * manually recreate)
 *
 * Eventually we can use this to catch rendering changes, with very
 * little work added to the instrumentation test.
 */
public class Screenshot {
  /**
   * Take a snapshot of an already measured and layout-ed view. See
   * adb-logcat for how to pull the screenshot.
   *
   * This method is thread safe.
   */
  public static RecordBuilder snap(View measuredView) {
    return ScreenshotImpl.getInstance().snap(measuredView);
  }

  /**
   * Take a snapshot of the activity and store it with the the
   * testName. See the adb-logcat for how to pull the screenshot.
   *
   * This method is thread safe.
   */
  public static RecordBuilder snapActivity(Activity activity) {
    return ScreenshotImpl.getInstance().snapActivity(activity);
  }
}
