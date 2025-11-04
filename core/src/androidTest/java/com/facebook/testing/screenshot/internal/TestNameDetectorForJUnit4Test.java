/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
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

package com.facebook.testing.screenshot.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/** Tests {@link TestNameDetector} (for JUnit4 style tests) */
public class TestNameDetectorForJUnit4Test {
  @Test
  public void testTestNameIsDetectedOnNonUiThread() throws Throwable {
    assertThat(TestNameDetector.getTestName()).isEqualTo("testTestNameIsDetectedOnNonUiThread");
    assertThat(TestNameDetector.getTestClass())
        .isEqualTo("com.facebook.testing.screenshot.internal.TestNameDetectorForJUnit4Test");
  }

  @Test
  public void testDelegated() throws Throwable {
    delegate(true);
    delegatePrivate();
  }

  public void delegate(boolean foobar) {
    assertThat(TestNameDetector.getTestName()).isEqualTo("testDelegated");
  }

  private void delegatePrivate() {
    assertThat(TestNameDetector.getTestName()).isEqualTo("testDelegated");
  }
}
