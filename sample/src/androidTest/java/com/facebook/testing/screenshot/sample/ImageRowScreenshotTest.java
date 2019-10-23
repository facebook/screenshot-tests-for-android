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

package com.facebook.testing.screenshot.sample;

import android.content.Context;
import android.view.LayoutInflater;
import androidx.test.platform.app.InstrumentationRegistry;
import com.facebook.litho.LithoView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;
import org.junit.Test;

public class ImageRowScreenshotTest {
  @Test
  public void testDefault() {
    Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    LayoutInflater inflater = LayoutInflater.from(targetContext);
    LithoView view = (LithoView) inflater.inflate(R.layout.litho_view, null, false);

    view.setComponent(ImageRow.create(view.getComponentContext()).build());

    ViewHelpers.setupView(view).setExactWidthDp(300).layout();
    Screenshot.snap(view).record();
  }
}
