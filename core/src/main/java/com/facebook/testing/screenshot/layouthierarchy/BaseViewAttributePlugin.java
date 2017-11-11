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
