/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */

package com.facebook.testing.screenshot.layouthierarchy;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractAttributePlugin implements AttributePlugin {
  protected String prefix(String name) {
    String prefix = namespace();
    if (TextUtils.isEmpty(prefix)) {
      return name;
    }
    return prefix + ":" + name;
  }

  protected void put(JSONObject node, String key, String value) throws JSONException {
    node.put(prefix(key), value);
  }

  protected void putPlain(JSONObject node, String key, String value) throws JSONException {
    node.put(key, value);
  }

  protected void putRequired(
      JSONObject node, String name, int left, int top, int width, int height) throws JSONException {
    node.put(KEY_CLASS, name);
    node.put(KEY_LEFT, left);
    node.put(KEY_TOP, top);
    node.put(KEY_WIDTH, width);
    node.put(KEY_HEIGHT, height);
  }
}
