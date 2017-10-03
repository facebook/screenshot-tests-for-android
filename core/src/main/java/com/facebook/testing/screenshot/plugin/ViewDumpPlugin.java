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

import java.util.Map;

/**
 * A plugin to get more metadata about a View.
 *
 * When screenshots are generated we use all registered plugins to
 * generate metadata for each of the views in the hierarchy.
 */
public interface ViewDumpPlugin {
  void dump(View view, Map<String, String> output);
}
