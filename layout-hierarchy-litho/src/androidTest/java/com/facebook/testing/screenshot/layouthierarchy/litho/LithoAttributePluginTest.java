/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.layouthierarchy.litho;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import com.facebook.litho.LithoView;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/** Tests {@link LithoAttributePlugin} */
@RunWith(AndroidJUnit4.class)
public class LithoAttributePluginTest {
  @Test
  public void testAcceptsLithoView() throws Exception {
    LithoView lithoView = new LithoView(InstrumentationRegistry.getTargetContext());
    assertEquals(true, LithoAttributePlugin.getInstance().accept(lithoView));
  }

  @Test
  public void testDoesntAcceptOtherViews() {
    ImageView imageView = new ImageView(InstrumentationRegistry.getTargetContext());
    assertEquals(false, LithoAttributePlugin.getInstance().accept(imageView));
  }
}
