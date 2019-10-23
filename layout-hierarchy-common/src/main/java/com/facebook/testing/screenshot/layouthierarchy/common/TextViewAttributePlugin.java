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

package com.facebook.testing.screenshot.layouthierarchy.common;

import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import com.facebook.testing.screenshot.layouthierarchy.AbstractAttributePlugin;
import org.json.JSONException;
import org.json.JSONObject;

/** Provides attribute details from a TextView */
public class TextViewAttributePlugin extends AbstractAttributePlugin {
  private static final String NAMESPACE = "TextView";
  private static final String TEXT = "text";
  private static final String TEXT_SIZE = "textSize";
  private static final String TEXT_COLOR = "textColor";
  private static final String TEXT_ALIGNMENT = "textAlignment";

  private static TextViewAttributePlugin INSTANCE = new TextViewAttributePlugin();

  public static TextViewAttributePlugin getInstance() {
    return INSTANCE;
  }

  private TextViewAttributePlugin() {
    // Only one instance
  }

  @Override
  public boolean accept(Object obj) {
    return obj instanceof TextView;
  }

  @Override
  public String namespace() {
    return NAMESPACE;
  }

  @Override
  public void putAttributes(JSONObject node, Object obj, Point offset) throws JSONException {
    if (!accept(obj)) {
      return;
    }
    final TextView textView = (TextView) obj;

    try {
      CharSequence text = textView.getText();
      if (text != null) {
        put(node, TEXT, text.toString());
      } else {
        put(node, TEXT, "null");
      }
    } catch (Exception e) {
      put(node, TEXT, e.getMessage());
    }

    put(node, TEXT_SIZE, String.valueOf(textView.getTextSize()));
    put(node, TEXT_COLOR, Integer.toHexString(textView.getCurrentTextColor()));
    if (Build.VERSION.SDK_INT >= 17) {
      put(node, TEXT_ALIGNMENT, nameForAlignment(textView.getTextAlignment()));
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
