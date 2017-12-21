/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot;

import android.test.InstrumentationTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * This is not really a test, this test is just a "fixture" for all the tests for the scripts
 * related to running tests and getting screenshots.
 */
public class ScriptsFixtureTest extends InstrumentationTestCase {
  private static final int HEIGHT = 100;
  private static final int WIDTH = 200;

  private TextView mTextView;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    mTextView = new TextView(getInstrumentation().getTargetContext());
    mTextView.setText("foobar");

    // Unfortunately TextView needs a LayoutParams for onDraw
    mTextView.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    measureAndLayout();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testGetTextViewScreenshot() throws Throwable {
    Screenshot.snap(mTextView).record();
  }

  public void testSecondScreenshot() throws Throwable {
    mTextView.setText("foobar3");
    measureAndLayout();
    Screenshot.snap(mTextView).record();
  }

  private void measureAndLayout() {
    try {
      runTestOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              mTextView.measure(
                  View.MeasureSpec.makeMeasureSpec(WIDTH, View.MeasureSpec.EXACTLY),
                  View.MeasureSpec.makeMeasureSpec(HEIGHT, View.MeasureSpec.EXACTLY));
              mTextView.layout(0, 0, mTextView.getMeasuredWidth(), mTextView.getMeasuredHeight());
            }
          });
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
