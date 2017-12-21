/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.internal;

import static org.mockito.Mockito.*;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

/** Tests {@link RecordBuilderImpl} */
public class RecordBuilderImplTest extends AndroidTestCase {
  private ScreenshotImpl mScreenshotImpl;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    mScreenshotImpl = mock(ScreenshotImpl.class);
  }

  public void testIncompleteTiles() throws Throwable {
    RecordBuilderImpl recordBuilder =
        new RecordBuilderImpl(mScreenshotImpl).setTiling(new Tiling(3, 4));

    try {
      recordBuilder.record();
      fail("expected exception");
    } catch (IllegalStateException e) {
      MoreAsserts.assertMatchesRegex(".*tiles.*", e.getMessage());
    }
  }

  public void testCompleteTiles() throws Throwable {
    RecordBuilderImpl recordBuilder =
        new RecordBuilderImpl(mScreenshotImpl).setTiling(new Tiling(3, 4));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 4; j++) {
        recordBuilder.getTiling().setAt(i, j, "foobar");
      }
    }

    recordBuilder.record();
  }

  public void testWithErrorStillDoesntFail() throws Throwable {
    RecordBuilderImpl recordBuilder = new RecordBuilderImpl(mScreenshotImpl);

    recordBuilder.setError("foo");
    recordBuilder.record();
  }
}
