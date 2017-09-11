/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

import android.app.Instrumentation;
import android.os.Bundle;

import com.facebook.testing.screenshot.internal.Registry;
import com.facebook.testing.screenshot.internal.ScreenshotImpl;

/**
 * The ScreenshotRunner needs to be called from the top level
 * Instrumentation runner before and after all the tests run.
 * <p>
 * You don't need to call this directly if you're using {@code
 * ScreenshotTestRunner} as your instrumentation.
 */
public abstract class ScreenshotRunner {

  /**
   * These strings can be used as Keys to Bundle Arguments.
   */
  public static final String SDCARD_DIRECTORY = "sdcard_directory";

  /**
   * Call this exactly once in your process before any screenshots are
   * generated.
   * <p>
   * Typically this will be in {@code InstrumentationTestRunner#onCreate()}
   */
  public static void onCreate(Instrumentation instrumentation, Bundle arguments) {
    Registry registry = Registry.getRegistry();
    registry.instrumentation = instrumentation;
    registry.arguments = arguments;
  }

  /**
   * Call this exactly once after all your tests have run.
   * <p>
   * Typically this can be in {@code InstrumentationTestRunner#finish()}
   */
  public static void onDestroy() {
    if (ScreenshotImpl.hasBeenCreated()) {
      ScreenshotImpl.getInstance().flush();
    }

    Registry.clear();
  }
}
