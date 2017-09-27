/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import static org.mockito.Mockito.*;

/**
 * Tests {@link RecordBuilderImpl}
 */
public class RecordBuilderImplTest extends AndroidTestCase {

  private ScreenshotImpl mScreenshotImpl;
  private ScreenshotNameCalculator calculator;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    mScreenshotImpl = mock(ScreenshotImpl.class);
    calculator = mock(ScreenshotNameCalculator.class);
  }

  public void testIncompleteTiles() throws Throwable {
    RecordBuilderImpl recordBuilder = new RecordBuilderImpl(mScreenshotImpl, calculator)
      .setTiling(new Tiling(3, 4));

    try {
      recordBuilder.record();
      fail("expected exception");
    } catch (IllegalStateException e) {
      MoreAsserts.assertMatchesRegex(".*tiles.*", e.getMessage());
    }
  }

  public void testCompleteTiles() throws Throwable {
    RecordBuilderImpl recordBuilder = new RecordBuilderImpl(mScreenshotImpl, calculator)
      .setTiling(new Tiling(3, 4));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 4; j++) {
        recordBuilder.getTiling().setAt(i, j, "foobar");
      }
    }

    recordBuilder.record();
  }

  public void testWithErrorStillDoesntFail() throws Throwable {
    RecordBuilderImpl recordBuilder = new RecordBuilderImpl(mScreenshotImpl, calculator);

    recordBuilder.setError("foo");
    recordBuilder.record();
  }
}
