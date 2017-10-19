/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.plugin;

import android.os.Build;
import android.view.View;
import android.widget.TextView;
import java.util.Map;

/** Dumps useful details from a TextView */
public class TextViewDumper extends ViewDumpPlugin {
  static final String TEXT = "text";
  static final String TEXT_SIZE = "textSize";
  static final String TEXT_COLOR = "textColor";
  static final String TEXT_ALIGNMENT = "textAlignment";

  private static TextViewDumper INSTANCE;

  public static TextViewDumper getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TextViewDumper();
    }
    return INSTANCE;
  }

  private TextViewDumper() {
    // Only one instance
  }

  @Override
  public String attributeNamespace() {
    return "TextView";
  }

  @Override
  public void dump(View view, Map<String, String> output) {
    if (!(view instanceof TextView)) {
      return;
    }
    final TextView textView = (TextView) view;

    try {
      CharSequence text = textView.getText();
      if (text != null) {
        put(output, TEXT, text.toString());
      } else {
        put(output, TEXT, "null");
      }
    } catch (Exception e) {
      put(output, TEXT, e.getMessage());
    }

    put(output, TEXT_SIZE, String.valueOf(textView.getTextSize()));
    put(output, TEXT_COLOR, Integer.toHexString(textView.getCurrentTextColor()));
    if (Build.VERSION.SDK_INT >= 17) {
      put(output, TEXT_ALIGNMENT, nameForAlignment(textView.getTextAlignment()));
    }
  }

  private static String nameForAlignment(int alignment) {
    switch (alignment) {
      case View.TEXT_ALIGNMENT_CENTER:
        return "TEXT_ALIGNMENT_CENTER";
      case View.TEXT_ALIGNMENT_GRAVITY:
        return "TEXT_ALIGNMENT_GRAVITY";
      case View.TEXT_ALIGNMENT_INHERIT:
        return "TEXT_ALIGNMENT_INHERIT";
      case View.TEXT_ALIGNMENT_TEXT_END:
        return "TEXT_ALIGNMENT_TEXT_END";
      case View.TEXT_ALIGNMENT_TEXT_START:
        return "TEXT_ALIGNMENT_TEXT_START";
      case View.TEXT_ALIGNMENT_VIEW_END:
        return "TEXT_ALIGNMENT_VIEW_END";
      case View.TEXT_ALIGNMENT_VIEW_START:
        return "TEXT_ALIGNMENT_VIEW_START";
      default:
        return "Unknown value";
    }
  }
}
