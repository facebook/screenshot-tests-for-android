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

package com.facebook.testing.screenshot.layouthierarchy.litho;

import android.graphics.Point;
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

  private void dumpHierarchy(
      LayoutHierarchyDumper dumper, JSONObject root, DebugComponent component, Point offset)
      throws JSONException {
    JSONArray children = new JSONArray();
    for (DebugComponent child : component.getChildComponents()) {
      children.put(dumper.dumpHierarchy(child, offset));
    }
    root.put(BaseViewHierarchyPlugin.KEY_CHILDREN, children);
  }
}
