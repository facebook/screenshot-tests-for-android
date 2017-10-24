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
