/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.layouthierarchy;

import android.graphics.Point;
import android.os.Build;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Dumps information about a layout hierarchy into a JSON object */
public final class LayoutHierarchyDumper {
  private static final List<AttributePlugin> sGlobalAttributePlugins = new ArrayList<>();
  private static final List<HierarchyPlugin> sGlobalHierarchyPlugins = new ArrayList<>();

  private final List<AttributePlugin> mAttributePlugins = new ArrayList<>();
  private final List<HierarchyPlugin> mHierarchyPlugins = new ArrayList<>();

  public static void addGlobalAttributePlugin(AttributePlugin plugin) {
    sGlobalAttributePlugins.add(plugin);
  }

  public static void removeGlobalAttributePlugin(AttributePlugin plugin) {
    sGlobalAttributePlugins.remove(plugin);
  }

  public static void addGlobalHierarchyPlugin(HierarchyPlugin plugin) {
    sGlobalHierarchyPlugins.add(plugin);
  }

  public static void removeGlobalHierarchyPlugin(HierarchyPlugin plugin) {
    sGlobalHierarchyPlugins.remove(plugin);
  }

  public static LayoutHierarchyDumper create() {
    return createWith(
        Collections.<HierarchyPlugin>emptyList(), Collections.<AttributePlugin>emptyList());
  }

  public static LayoutHierarchyDumper createWith(
      List<HierarchyPlugin> hierarchyPlugins, List<AttributePlugin> attributePlugins) {
    final List<HierarchyPlugin> allHierarchyPlugins = new ArrayList<>(hierarchyPlugins.size() + 1);
    allHierarchyPlugins.addAll(hierarchyPlugins);
    allHierarchyPlugins.addAll(sGlobalHierarchyPlugins);
    allHierarchyPlugins.add(BaseViewHierarchyPlugin.getInstance());

    final List<AttributePlugin> allAttributePlugins = new ArrayList<>(attributePlugins.size() + 1);
    allAttributePlugins.add(BaseViewAttributePlugin.getInstance());
    allAttributePlugins.addAll(attributePlugins);
    allAttributePlugins.addAll(sGlobalAttributePlugins);

    return new LayoutHierarchyDumper(allHierarchyPlugins, allAttributePlugins);
  }

  LayoutHierarchyDumper(
      List<HierarchyPlugin> hierarchyPlugins, List<AttributePlugin> attributePlugins) {
    mHierarchyPlugins.addAll(hierarchyPlugins);
    mAttributePlugins.addAll(attributePlugins);
  }

  public JSONObject dumpAttributes(Object obj, Point offset) throws JSONException {
    JSONObject node = new JSONObject();
    for (AttributePlugin plugin : mAttributePlugins) {
      if (plugin.accept(obj)) {
        plugin.putAttributes(node, obj, offset);
      }
    }
    return node;
  }

  public JSONObject dumpHierarchy(View view) throws JSONException {
    Point offset = new Point(-getViewLeft(view), -getViewTop(view));
    return dumpHierarchy(view, offset);
  }

  public JSONObject dumpHierarchy(Object obj, Point offset) throws JSONException {
    JSONObject node = dumpAttributes(obj, offset);
    for (HierarchyPlugin plugin : mHierarchyPlugins) {
      if (plugin.accept(obj)) {
        // First hierarchy wins
        plugin.putHierarchy(this, node, obj, offset);
        return node;
      }
    }
    throw new IllegalStateException(
        "No available plugins for type " + obj.getClass().getCanonicalName());
  }

  public static int getViewLeft(View view) {
    if (Build.VERSION.SDK_INT >= 11) {
      return view.getLeft() + (int) view.getTranslationX();
    }
    return view.getLeft();
  }

  public static int getViewTop(View view) {
    if (Build.VERSION.SDK_INT >= 11) {
      return view.getTop() + (int) view.getTranslationY();
    }
    return view.getTop();
  }
}
