/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * ViewPagers need a patch to work with screenshot tests. This test
 * verifies that the patch is doing its job.
 */
@RunWith(AndroidJUnit4.class)
public class ViewPagerScreenshotTest {
  @Test
  public void testViewPagerStuff() throws Throwable {
    Context context = InstrumentationRegistry.getTargetContext();
    ViewPager viewPager = new ViewPager(context);
    viewPager.setAdapter(mPagerAdapter);

    WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(viewPager);
    Bitmap bmp = ViewHelpers.setupView(viewPager)
      .setExactHeightPx(200)
      .setExactWidthPx(200)
      .layout()
      .draw();
    detacher.detach();

    for (int i = 0; i < 200; i++) {
      for (int j = 0; j < 200; j++) {
        assertEquals(
            String.format("must be red at %d, %d", i, j),
            Color.RED,
            bmp.getPixel(i, j));
      }
    }
  }

  private final PagerAdapter mPagerAdapter = new PagerAdapter() {
      @Override
      public int getCount() {
        return 1;
      }

      @Override
      public Object instantiateItem(ViewGroup container, int position) {
        View view = new View(container.getContext());
        view.setBackgroundColor(Color.RED);
        container.addView(view);
        return view;
      }

      @Override
      public boolean isViewFromObject(View view, Object obj) {
        return true;
      }
    };
}
