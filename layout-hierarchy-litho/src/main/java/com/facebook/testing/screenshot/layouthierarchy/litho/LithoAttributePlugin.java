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
import com.facebook.testing.screenshot.layouthierarchy.AbstractAttributePlugin;

import org.json.JSONException;
import org.json.JSONObject;

public class LithoAttributePlugin extends AbstractAttributePlugin {
  private static final LithoAttributePlugin INSTANCE = new LithoAttributePlugin();

  public static LithoAttributePlugin getInstance() {
    return INSTANCE;
  }

  private LithoAttributePlugin() {
    // Single instance
  }

  @Override
  public boolean accept(Object obj) {
    return obj instanceof LithoView || obj instanceof DebugComponent;
  }

  @Override
  public String namespace() {
    return "Litho";
  }

  @Override
  public void putAttributes(JSONObject node, Object obj, Point offset) throws JSONException {
    final DebugComponent debugComponent;
    if (obj instanceof LithoView) {
      ((LithoView) obj).rebind();
      debugComponent = DebugComponent.getRootInstance((LithoView) obj);
      if (debugComponent == null || !debugComponent.isValidInstance()) {
        return;
      }
    } else {
      debugComponent = (DebugComponent) obj;

      // Since we're dealing with a pure component, we cant rely on the default required
      // attributes to be added, so we add them here
      Rect bounds = debugComponent.getBoundsInLithoView();
      putRequired(
          node,
          debugComponent.getName(),
          offset.x + bounds.left,
          offset.y + bounds.top,
          bounds.width(),
          bounds.height());
    }
  }
}
