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
import org.json.JSONException;
import org.json.JSONObject;

/** Dumps basic information that applies to all {@link View}s, like position and class */
public class BaseViewAttributePlugin extends AbstractAttributePlugin {
  private static final BaseViewAttributePlugin INSTANCE = new BaseViewAttributePlugin();

  public static BaseViewAttributePlugin getInstance() {
    return INSTANCE;
  }

  private BaseViewAttributePlugin() {
    // Single instance
  }

  @Override
  public boolean accept(Object obj) {
    return obj instanceof View;
  }

  @Override
  public String namespace() {
    return "";
  }

  @Override
  public void putAttributes(JSONObject node, Object obj, Point offset) throws JSONException {
    final View view = (View) obj;
    putRequired(
        node,
        view.getClass().getCanonicalName(),
        offset.x + LayoutHierarchyDumper.getViewLeft(view),
        offset.y + LayoutHierarchyDumper.getViewTop(view),
        view.getWidth(),
        view.getHeight());
  }
}
