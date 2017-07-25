/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link TestNameDetector} (for JUnit4 style tests)
 */
public class TestNameDetectorForJUnit4Test {
  @Test
  public void testTestNameIsDetectedOnNonUiThread() throws Throwable {
    assertEquals("testTestNameIsDetectedOnNonUiThread", TestNameDetector.getTestName());
    assertEquals(
        "com.facebook.testing.screenshot.internal.TestNameDetectorForJUnit4Test",
        TestNameDetector.getTestClass());
  }

  @Test
  public void testDelegated() throws Throwable {
    delegate(true);
    delegatePrivate();
  }

  public void delegate(boolean foobar) {
    assertEquals("testDelegated", TestNameDetector.getTestName());
  }

  private void delegatePrivate() {
    assertEquals("testDelegated", TestNameDetector.getTestName());
  }
}
