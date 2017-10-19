/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.testing.screenshot.plugin.PluginRegistry;
import com.facebook.testing.screenshot.plugin.ViewDumpPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Dumps information about the view hierarchy into a JSON object
 */
public class ViewHierarchy {
  public static final String KEY_CLASS = "class";
  public static final String KEY_X = "x";
  public static final String KEY_Y = "y";
  public static final String KEY_WIDTH = "width";
  public static final String KEY_HEIGHT = "height";
  public static final String KEY_CHILDREN = "children";

  private ViewHierarchy() {
    throw new AssertionError("No instances");
  }

  public static JSONObject dump(View view) throws JSONException {
    final JSONObject root;
    Point offset = new Point(-getLeft(view), -getTop(view));
    if (view instanceof ViewGroup) {
      root = dumpGroup((ViewGroup) view, offset);
    } else {
      root = dumpView(view, offset);
    }
    return root;
  }

  private static JSONObject dumpGroup(ViewGroup viewGroup, Point offset) throws JSONException {
    JSONObject node = dumpView(viewGroup, offset);
    final int offsetX = getLeft(viewGroup);
    final int offsetY = getTop(viewGroup);
    offset.offset(offsetX, offsetY);

    JSONArray children = new JSONArray();
    for (int i = 0, size = viewGroup.getChildCount(); i < size; ++i) {
      View child = viewGroup.getChildAt(i);
      if (child instanceof ViewGroup) {
        children.put(dumpGroup((ViewGroup) child, offset));
      } else {
        children.put(dumpView(child, offset));
      }
    }
    node.put(KEY_CHILDREN, children);

    offset.offset(-offsetX, -offsetY);
    return node;
  }

  private static JSONObject dumpView(View view, Point offset) throws JSONException {
    JSONObject node = new JSONObject();
    node.put(KEY_CLASS, view.getClass().getCanonicalName());
    node.put(KEY_X, offset.x + getLeft(view));
    node.put(KEY_Y, offset.y + getTop(view));
    node.put(KEY_WIDTH, view.getWidth());
    node.put(KEY_HEIGHT, view.getHeight());

    Map<String, String> extraValues = new HashMap<>();
    for (ViewDumpPlugin plugin : PluginRegistry.getPlugins()) {
      plugin.dump(view, extraValues);
    }
    for (Map.Entry<String, String> extra : extraValues.entrySet()) {
      node.put(extra.getKey(), extra.getValue());
    }
    return node;
  }

  private static int getLeft(View view) {
    return view.getLeft() + (int) view.getTranslationX();
  }

  private static int getTop(View view) {
    return view.getTop() + (int) view.getTranslationY();
  }
}

