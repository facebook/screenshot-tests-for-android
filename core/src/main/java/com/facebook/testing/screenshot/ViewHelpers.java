/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ListView;

import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * A collection of static utilities for measuring and pre-drawing a
 * view, usually a pre-requirement for taking a Screenshot.
 * <p>
 * This will mostly be used something like this:
 * <p>
 * <code>
 * ViewHelpers.setupView(view)
 * .setExactHeightPx(1000)
 * .setExactWidthPx(100)
 * .layout();
 * </code>
 */
public class ViewHelpers {
  private static final int HEIGHT_LIMIT = 100000;

  private View mView;
  private int mWidthMeasureSpec;
  private int mHeightMeasureSpec;
  private boolean mGuessListViewHeight;

  private ViewHelpers(View view) {
    mView = view;

    mWidthMeasureSpec = makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    mHeightMeasureSpec = makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
  }

  /**
   * Start setup for a view, see class documentation for details.
   */
  public static ViewHelpers setupView(View view) {
    return new ViewHelpers(view);
  }

  /**
   * Measure and layout the view after all the configuration is done.
   *
   * @returns an AfterLayout object that can be used to perform common
   * operations after the layout is done such as dispatchPreDraw()l.
   */
  public AfterLayout layout() {
    if (!mGuessListViewHeight) {
      layoutInternal();
    } else {
      layoutWithHeightDetection();
    }
    dispatchPreDraw(mView);
    return new AfterLayout();
  }

  private void layoutInternal() {
    do {
      mView.measure(
          mWidthMeasureSpec,
          mHeightMeasureSpec);
      layoutView();
    } while (mView.isLayoutRequested());
  }

  private void layoutWithHeightDetection() {
    ListView view = (ListView) mView;
    mHeightMeasureSpec = makeMeasureSpec(HEIGHT_LIMIT, MeasureSpec.EXACTLY);
    layoutInternal();

    if (view.getCount() != view.getChildCount()) {
      throw new IllegalStateException("the ListView is too big to be auto measured");
    }

    int bottom = 0;

    if (view.getCount() > 0) {
      bottom = view.getChildAt(view.getCount() - 1).getBottom();
    }

    if (bottom == 0) {
      bottom = 1;
    }

    mHeightMeasureSpec = makeMeasureSpec(bottom, MeasureSpec.EXACTLY);
    layoutInternal();
  }

  /**
   * Configure the height in pixel
   */
  public ViewHelpers setExactHeightPx(int px) {
    mHeightMeasureSpec = makeMeasureSpec(px, MeasureSpec.EXACTLY);
    validateHeight();
    return this;
  }

  public ViewHelpers guessListViewHeight() {
    if (!(mView instanceof ListView)) {
      throw new IllegalArgumentException("guessListViewHeight needs to be used with a ListView");
    }
    mGuessListViewHeight = true;
    validateHeight();
    return this;
  }

  private void validateHeight() {
    if (mGuessListViewHeight && mHeightMeasureSpec != 0) {
      throw new IllegalStateException("Can't call both setExactHeight && guessListViewHeight");
    }
  }

  /**
   * Configure the width in pixels
   */
  public ViewHelpers setExactWidthPx(int px) {
    mWidthMeasureSpec = makeMeasureSpec(px, MeasureSpec.EXACTLY);
    return this;
  }

  /**
   * Configure the height in dip
   */
  public ViewHelpers setExactWidthDp(int dp) {
    setExactWidthPx(dpToPx(dp));
    return this;
  }

  /**
   * Configure the width in dip
   */
  public ViewHelpers setExactHeightDp(int dp) {
    setExactHeightPx(dpToPx(dp));
    return this;
  }

  /**
   * Some views (e.g. SimpleVariableTextLayoutView) in FB4A rely on
   * the predraw. Actually I don't know why, ideally it shouldn't.
   * <p>
   * However if you find that text is not showing in your layout, try
   * dispatching the pre draw using this method. Note this method is
   * only supported for views that are not attached to a Window, and
   * the behavior is slightly different than views attached to a
   * window. (Views attached to a window have a single
   * ViewTreeObserver for all child views, whereas for unattached
   * views, each child has its own ViewTreeObserver.)
   */
  private void dispatchPreDraw(View view) {
    while (view.getViewTreeObserver().dispatchOnPreDraw()) {
    }

    if (view instanceof ViewGroup) {
      ViewGroup vg = (ViewGroup) view;
      for (int i = 0; i < vg.getChildCount(); i++) {
        dispatchPreDraw(vg.getChildAt(i));
      }
    }
  }

  private void layoutView() {
    mView.layout(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());
  }

  private int dpToPx(int dp) {
    Resources resources = mView.getContext().getResources();
    return (int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
  }

  public class AfterLayout {
    public Bitmap draw() {
      WindowAttachment.Detacher detacher = WindowAttachment.dispatchAttach(mView);
      try {
        Bitmap bmp = Bitmap.createBitmap(
            mView.getWidth(), mView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        mView.draw(canvas);
        return bmp;
      } finally {
        detacher.detach();
      }
    }
  }
}
