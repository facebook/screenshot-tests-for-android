/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;

/**
 * Tests {@link TestNameDetector}
 */
public class TestNameDetectorTest extends InstrumentationTestCase {
  @UiThreadTest
  public void testTestNameIsDetected() throws Throwable {
    assertEquals("testTestNameIsDetected", TestNameDetector.getTestName());
    assertEquals(
      "com.facebook.testing.screenshot.internal.TestNameDetectorTest",
      TestNameDetector.getTestClass());
  }

  public void testTestNameIsDetectedOnNonUiThread() throws Throwable {
    assertEquals("testTestNameIsDetectedOnNonUiThread", TestNameDetector.getTestName());
    assertEquals(
      "com.facebook.testing.screenshot.internal.TestNameDetectorTest",
      TestNameDetector.getTestClass());
  }
}
