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
      if (debugComponent == null) {
        return;
      }
    } else {
      debugComponent = (DebugComponent) obj;

      // Since we're dealing with a pure component, we cant rely on the default required
      // attributes to be added, so we add them here
      Rect bounds = debugComponent.getBoundsInLithoView();
      putRequired(
          node,
          debugComponent.getComponent().getClass().getName(),
          offset.x + bounds.left,
          offset.y + bounds.top,
          bounds.width(),
          bounds.height());
    }
  }
}
