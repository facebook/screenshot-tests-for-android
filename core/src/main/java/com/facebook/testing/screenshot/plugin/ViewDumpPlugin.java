/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.plugin;

import android.text.TextUtils;
import android.view.View;
import java.util.Map;

/**
 * A plugin to get more metadata about a View.
 *
 * <p>When screenshots are generated we use all registered plugins to generate metadata for each of
 * the views in the hierarchy.
 */
public abstract class ViewDumpPlugin {
  public abstract void dump(View view, Map<String, String> output);

  public String attributeNamespace() {
    return "";
  }

  public String prefix(String name) {
    String prefix = attributeNamespace();
    if (TextUtils.isEmpty(prefix)) {
      return name;
    }
    return prefix + ":" + name;
  }

  public void put(Map<String, String> output, String key, String value) {
    output.put(prefix(key), value);
  }
}
