/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the license found in the LICENSE-examples file in the root
 * directory of this source tree.
 */
package com.facebook.testing.screenshot.sample;

import android.content.Context;
import android.view.LayoutInflater;
import androidx.test.platform.app.InstrumentationRegistry;
import com.facebook.litho.LithoView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;
import org.junit.Test;

public class ExampleScreenshotTest {
  @Test
  public void testDefault() {
    Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    LayoutInflater inflater = LayoutInflater.from(targetContext);
    LithoView view = (LithoView) inflater.inflate(R.layout.litho_view, null, false);

    view.setComponent(Example.create(view.getComponentContext()).build());

    ViewHelpers.setupView(view).setExactWidthDp(300).layout();
    Screenshot.snap(view).record();
  }
}
