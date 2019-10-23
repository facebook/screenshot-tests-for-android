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
import android.view.View;
import android.view.ViewGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseViewHierarchyPlugin implements HierarchyPlugin {
  private static BaseViewHierarchyPlugin INSTANCE = new BaseViewHierarchyPlugin();

  public static BaseViewHierarchyPlugin getInstance() {
    return INSTANCE;
  }

  private BaseViewHierarchyPlugin() {
    // Stateless, no instances
  }

  @Override
  public boolean accept(Object obj) {
    return obj instanceof View;
  }

  @Override
  public void putHierarchy(LayoutHierarchyDumper dumper, JSONObject root, Object view, Point offset)
      throws JSONException {
    if (!(view instanceof ViewGroup)) {
      return;
    }

    ViewGroup group = (ViewGroup) view;
    final int offsetLeft = LayoutHierarchyDumper.getViewLeft(group);
    final int offsetTop = LayoutHierarchyDumper.getViewTop(group);
    offset.offset(offsetLeft, offsetTop);

    JSONArray children = new JSONArray();
    for (int i = 0, size = group.getChildCount(); i < size; ++i) {
      View child = group.getChildAt(i);
      children.put(dumper.dumpHierarchy(child, offset));
    }

    root.put(KEY_CHILDREN, children);
    offset.offset(-offsetLeft, -offsetTop);
  }
}
