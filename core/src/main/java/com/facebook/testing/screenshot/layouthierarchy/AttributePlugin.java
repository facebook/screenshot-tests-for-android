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

import android.graphics.Point;
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
