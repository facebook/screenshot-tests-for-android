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

  protected void putRequired(JSONObject node, String name, int left, int top, int width, int height)
      throws JSONException {
    node.put(KEY_CLASS, name);
    node.put(KEY_LEFT, left);
    node.put(KEY_TOP, top);
    node.put(KEY_WIDTH, width);
    node.put(KEY_HEIGHT, height);
  }
}
