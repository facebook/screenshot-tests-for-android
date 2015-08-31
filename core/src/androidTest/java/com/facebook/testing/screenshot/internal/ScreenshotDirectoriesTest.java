/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import java.io.File;

import android.content.Context;
import org.junit.Test;
import org.junit.After;
import android.support.test.InstrumentationRegistry;

import static org.junit.Assert.*;

public class ScreenshotDirectoriesTest {
  File mDir;

  @After
  public void teardown() throws Exception {
    if (mDir != null) {
      mDir.delete();
    }
  }

  @Test
  public void testUsesSdcard() {
    Context context = InstrumentationRegistry.getTargetContext();
    ScreenshotDirectories dirs = new ScreenshotDirectories(context);

    mDir = dirs.get("foobar");
    assertTrue(mDir.exists());
  }
}
