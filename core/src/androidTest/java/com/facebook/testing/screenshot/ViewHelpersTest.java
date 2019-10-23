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

package com.facebook.testing.screenshot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.test.InstrumentationRegistry;
import com.facebook.testing.screenshot.test.R;
import org.junit.Before;

/** Tests {@link ViewHelpers} */
public class ViewHelpersTest {
  private TextView mTextView;
  private Context targetContext;

  @Before
  public void setUp() throws Exception {
    targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    mTextView = new TextView(targetContext);
    mTextView.setText("foobar");
  }

  public void testPreconditions() throws Throwable {
    assertEquals(0, mTextView.getMeasuredHeight());
  }

  public void testMeasureWithoutHeight() throws Throwable {
    ViewHelpers.setupView(mTextView).setExactWidthDp(100).layout();

    assertThat(mTextView.getMeasuredHeight(), greaterThan(0));
  }

  public void testMeasureWithoutHeightPx() throws Throwable {
    ViewHelpers.setupView(mTextView).setExactWidthPx(100).layout();

    assertThat(mTextView.getMeasuredHeight(), greaterThan(0));
  }

  public void testMeasureForOnlyWidth() throws Throwable {
    ViewHelpers.setupView(mTextView).setExactHeightPx(100).layout();

    assertThat(mTextView.getMeasuredHeight(), equalTo(100));
    assertThat(mTextView.getMeasuredWidth(), greaterThan(0));
  }

  public void testBothWrapContent() throws Throwable {
    ViewHelpers.setupView(mTextView).layout();

    assertThat(mTextView.getMeasuredHeight(), greaterThan(0));
    assertThat(mTextView.getMeasuredWidth(), greaterThan(0));
  }

  public void testHeightAndWidthCorrectlyPropagated() throws Throwable {
    ViewHelpers.setupView(mTextView).setExactHeightDp(100).setExactWidthDp(1000).layout();

    assertThat(mTextView.getMeasuredWidth(), greaterThan(mTextView.getMeasuredHeight()));
  }

  public void testListViewHeight() throws Throwable {
    ListView view = new ListView(targetContext);
    view.setDividerHeight(0);
    ArrayAdapter<String> adapter =
        new ArrayAdapter<String>(targetContext, R.layout.testing_simple_textview);
    view.setAdapter(adapter);

    for (int i = 0; i < 20; i++) {
      adapter.add("foo");
    }

    ViewHelpers.setupView(view).guessListViewHeight().setExactWidthDp(200).layout();

    assertThat(view.getMeasuredHeight(), greaterThan(10));

    int oneHeight = view.getChildAt(0).getMeasuredHeight();
    assertThat(view.getMeasuredHeight(), equalTo(oneHeight * 20));
  }

  public void testMaxHeightLessThanHeight() throws Throwable {
    ViewHelpers.setupView(mTextView).setMaxHeightPx(100).layout();
    assertThat(mTextView.getMeasuredHeight(), lessThan(100));
  }

  public void testMaxHeightUsesFullHeight() throws Throwable {
    ViewHelpers.setupView(mTextView).setMaxHeightPx(1).layout();
    assertThat(mTextView.getMeasuredHeight(), equalTo(1));
  }
}
