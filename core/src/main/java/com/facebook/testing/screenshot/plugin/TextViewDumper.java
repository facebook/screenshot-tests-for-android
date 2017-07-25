/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.plugin;

import android.view.View;
import android.widget.TextView;

import java.util.Map;

/**
 * Dumps useful details from a TextView
 */
public class TextViewDumper implements ViewDumpPlugin {
  @Override
  public void dump(View view, Map<String, String> output) {
    if (!(view instanceof TextView)) {
      return;
    }

    TextView tv = (TextView) view;

    CharSequence text;

    try {
      text = tv.getText();
    } catch (RuntimeException e) {
      // Somebody has a custom TextView that misbehaves
      text = "unsupported";
    }

    if (text == null) {
      text = "null";
    }
    output.put("text", text.toString());
  }
}
