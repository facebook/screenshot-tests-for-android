/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

import android.test.InstrumentationTestCase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.R;

public class SearchBarTest extends InstrumentationTestCase {
  public void testRendering() throws Throwable {
    LayoutInflater inflater = LayoutInflater.from(getInstrumentation().getTargetContext());
    View view = inflater.inflate(R.layout.search_bar, null, false);

    ViewHelpers.setupView(view)
      .setExactWidthDp(300)
      .layout();

    Screenshot.snap(view)
      .record();
  }

  public void testLongText() throws Throwable {
    LayoutInflater inflater = LayoutInflater.from(getInstrumentation().getTargetContext());
    View view = inflater.inflate(R.layout.search_bar, null, false);

    TextView tv = (TextView) view.findViewById(R.id.search_box);

    tv.setText("This is a really long text and should overflow");
    ViewHelpers.setupView(view)
      .setExactWidthDp(300)
      .layout();

    Screenshot.snap(view)
      .record();
  }

  public void testChinese() throws Throwable {
    LayoutInflater inflater = LayoutInflater.from(getInstrumentation().getTargetContext());
    View view = inflater.inflate(R.layout.search_bar, null, false);

    TextView tv = (TextView) view.findViewById(R.id.search_box);
    TextView btn = (TextView) view.findViewById(R.id.button);

    tv.setHint("搜索世界");
    btn.setText("搜");

    ViewHelpers.setupView(view)
      .setExactWidthDp(300)
      .layout();

    Screenshot.snap(view)
      .record();
  }
}
