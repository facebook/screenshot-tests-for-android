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
