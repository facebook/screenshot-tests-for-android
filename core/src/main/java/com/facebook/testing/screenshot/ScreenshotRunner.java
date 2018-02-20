/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot;

import android.app.Instrumentation;
import android.os.Bundle;
import com.facebook.testing.screenshot.internal.Registry;
import com.facebook.testing.screenshot.internal.ScreenshotImpl;

/**
 * The ScreenshotRunner needs to be called from the top level Instrumentation test runner before and
 * after all the tests run.
 */
public abstract class ScreenshotRunner {

  /** These strings can be used as Keys to Bundle Arguments. */
  public static final String SDCARD_DIRECTORY = "sdcard_directory";

  /**
   * Call this exactly once in your process before any screenshots are generated.
   *
   * <p>Typically this will be in {@code AndroidJUnitRunner#onCreate()}
   */
  public static void onCreate(Instrumentation instrumentation, Bundle arguments) {
    Registry registry = Registry.getRegistry();
    registry.instrumentation = instrumentation;
    registry.arguments = arguments;
  }

  /**
   * Call this exactly once after all your tests have run.
   *
   * <p>Typically this can be in {@code AndroidJUnitRunner#finish()}
   */
  public static void onDestroy() {
    if (ScreenshotImpl.hasBeenCreated()) {
      ScreenshotImpl.getInstance().flush();
    }

    Registry.clear();
  }
}
