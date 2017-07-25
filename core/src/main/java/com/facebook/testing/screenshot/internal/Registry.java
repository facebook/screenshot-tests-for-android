/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.app.Instrumentation;
import android.os.Bundle;

/**
 * Stores some of the static state. We bundle this into a class for
 * easy cleanup.
 */
public class Registry {
  public Instrumentation instrumentation;
  public Bundle arguments;

  private static Registry sRegistry;

  public static Registry getRegistry() {
    if (sRegistry == null) {
      sRegistry = new Registry();
    }

    return sRegistry;
  }

  public static void clear() {
    sRegistry = null;
  }
}
