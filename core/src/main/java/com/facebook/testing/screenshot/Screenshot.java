/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.testing.screenshot;

import android.app.Activity;
import android.view.View;
import com.facebook.testing.screenshot.internal.ScreenshotImpl;

/**
 * A testing tool for taking a screenshot during an Activity instrumentation test. This is really
 * useful while manually investigating how the rendering looks like after setting up some complex
 * set of conditions in the test. (Which might be hard to manually recreate)
 *
 * <p>Eventually we can use this to catch rendering changes, with very little work added to the
 * instrumentation test.
 */
public class Screenshot {
  /**
   * Take a snapshot of an already measured and layout-ed view. See adb-logcat for how to pull the
   * screenshot.
   *
   * <p>This method is thread safe.
   */
  public static RecordBuilder snap(View measuredView) {
    return ScreenshotImpl.getInstance().snap(measuredView);
  }

  /**
   * Take a snapshot of the activity and store it with the the testName. See the adb-logcat for how
   * to pull the screenshot.
   *
   * <p>This method is thread safe.
   */
  public static RecordBuilder snapActivity(Activity activity) {
    return ScreenshotImpl.getInstance().snapActivity(activity);
  }

  /** @return The largest amount of pixels we'll capture, otherwise an exception will be thrown. */
  public static long getMaxPixels() {
    return ScreenshotImpl.getMaxPixels();
  }
}
