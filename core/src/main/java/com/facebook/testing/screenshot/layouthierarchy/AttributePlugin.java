/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.layouthierarchy;

import android.graphics.Point;
import android.view.View;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A plugin for a layout hierarchy that allows you to configure what attributes are added per view
 * type.
 */
public interface AttributePlugin {
  String KEY_CLASS = "class";
  String KEY_LEFT = "left";
  String KEY_TOP = "top";
  String KEY_WIDTH = "width";
  String KEY_HEIGHT = "height";

  /** Determines whether this plugin operates on the given type */
  boolean accept(Object obj);

  /** Returns the namespace of the attributes this plugin inserts */
  String namespace();

  /** Puts all interesting attributes of the given object into the node */
  void putAttributes(JSONObject node, Object obj, Point offset) throws JSONException;
}
