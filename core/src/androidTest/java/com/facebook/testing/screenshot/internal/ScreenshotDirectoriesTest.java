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

import static org.junit.Assert.*;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import java.io.File;
import org.junit.After;
import org.junit.Test;

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
