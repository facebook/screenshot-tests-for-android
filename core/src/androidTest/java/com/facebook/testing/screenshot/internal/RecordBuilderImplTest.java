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

package com.facebook.testing.screenshot.internal;

import static org.mockito.Mockito.*;

import org.junit.Before;

/** Tests {@link RecordBuilderImpl} */
public class RecordBuilderImplTest {
  private ScreenshotImpl mScreenshotImpl;

  @Before
  public void setUp() throws Exception {
    mScreenshotImpl = mock(ScreenshotImpl.class);
  }

  public void testIncompleteTiles() throws Throwable {
    RecordBuilderImpl recordBuilder =
        new RecordBuilderImpl(mScreenshotImpl).setTiling(new Tiling(3, 4));

    try {
      recordBuilder.record();
      throw new IllegalStateException("expected exception");
    } catch (IllegalStateException e) {
      OldApiBandaid.assertMatchesRegex(".*tiles.*", e.getMessage());
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
