/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

import android.app.Activity;

/**
 * A dummy activity used in {@link BaseActivityInstrumentationTestCase2Test}
 */
public class MyActivity extends Activity {
  public boolean destroyed = false;

  @Override
  public void onDestroy() {
    super.onDestroy();
    destroyed = true;
  }
}
