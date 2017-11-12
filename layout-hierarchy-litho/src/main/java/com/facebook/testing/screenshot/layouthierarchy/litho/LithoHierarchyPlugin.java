/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.layouthierarchy.litho;

import android.graphics.Point;
import android.graphics.Rect;

import com.facebook.litho.DebugComponent;
import com.facebook.litho.LithoView;
import com.facebook.testing.screenshot.layouthierarchy.BaseViewHierarchyPlugin;
import com.facebook.testing.screenshot.layouthierarchy.HierarchyPlugin;
import com.facebook.testing.screenshot.layouthierarchy.LayoutHierarchyDumper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LithoHierarchyPlugin implements HierarchyPlugin {
  private static final LithoHierarchyPlugin INSTANCE = new LithoHierarchyPlugin();

  public static LithoHierarchyPlugin getInstance() {
    return INSTANCE;
  }

  private LithoHierarchyPlugin() {
    // Single instance
  }

  @Override
  public boolean accept(Object obj) {
    return obj instanceof LithoView || obj instanceof DebugComponent;
  }

  @Override
  public void putHierarchy(LayoutHierarchyDumper dumper, JSONObject root, Object obj, Point offset)
      throws JSONException {
    if (!accept(obj)) {
      return;
    }

    if (obj instanceof LithoView) {
      LithoView lithoView = (LithoView) obj;
      DebugComponent debugComponent = DebugComponent.getRootInstance(lithoView);
      if (debugComponent == null) {
        return;
      }
      final int offsetLeft = LayoutHierarchyDumper.getViewLeft(lithoView);
      final int offsetTop = LayoutHierarchyDumper.getViewTop(lithoView);
      offset.offset(offsetLeft, offsetTop);
      dumpHierarchy(dumper, root, debugComponent, offset);
      offset.offset(-offsetLeft, -offsetTop);
    } else {
      dumpHierarchy(dumper, root, (DebugComponent) obj, offset);
    }
  }

  private void dumpHierarchy(LayoutHierarchyDumper dumper, JSONObject root, DebugComponent component, Point offset) throws JSONException {
    JSONArray children = new JSONArray();
    for (DebugComponent child : component.getChildComponents()) {
      children.put(dumper.dumpHierarchy(child, offset));
    }
    root.put(BaseViewHierarchyPlugin.KEY_CHILDREN, children);
  }
}
