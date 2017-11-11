/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.layouthierarchy;

import android.graphics.Point;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A plugin for a layout hierarchy that allows you to configure how certain hierarchies are created.
 * If you have a custom view group, for example, that you want to display differently than normal,
 * then you would create a plugin for it.
 */
public interface HierarchyPlugin {
  String KEY_CHILDREN = "children";

  /** Determines whether this plugin operates on the given type */
  boolean accept(Object obj);

  /** Constructs the hierarchy of the given type into a {@link org.json.JSONObject} */
  void putHierarchy(LayoutHierarchyDumper dumper, JSONObject node, Object obj, Point offset)
      throws JSONException;
}
