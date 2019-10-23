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
