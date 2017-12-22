/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.internal;

import static org.junit.Assert.assertEquals;

import android.test.UiThreadTest;
import org.junit.Test;

/** Tests {@link TestNameDetector} */
public class TestNameDetectorTest {
  @Test
  @UiThreadTest
  public void testTestNameIsDetected() throws Throwable {
    assertEquals("testTestNameIsDetected", TestNameDetector.getTestName());
    assertEquals(
        "com.facebook.testing.screenshot.internal.TestNameDetectorTest",
        TestNameDetector.getTestClass());
  }

  @Test
  public void testTestNameIsDetectedOnNonUiThread() throws Throwable {
    assertEquals("testTestNameIsDetectedOnNonUiThread", TestNameDetector.getTestName());
    assertEquals(
        "com.facebook.testing.screenshot.internal.TestNameDetectorTest",
        TestNameDetector.getTestClass());
  }

  @Test
  @UiThreadTest
  public void testTestNameIsDetectedThroughExtraMethod() throws Throwable {
    extraLayerMethod();
  }

  private void extraLayerMethod() {
    assertEquals("testTestNameIsDetectedThroughExtraMethod", TestNameDetector.getTestName());
    assertEquals(
        "com.facebook.testing.screenshot.internal.TestNameDetectorTest",
        TestNameDetector.getTestClass());
  }
}
