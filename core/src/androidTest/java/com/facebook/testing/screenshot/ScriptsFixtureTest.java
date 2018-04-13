/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
