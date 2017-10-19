/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.facebook.testing.screenshot.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains a global list of {@code ViewDumpPlugin}s
 */
public class PluginRegistry {
  private static ArrayList<ViewDumpPlugin> sPlugins = new ArrayList<>();

  static {
    sPlugins.add(new TextViewDumper());
  }

  /**
   * Adds a new plugin
   */
  public static void addPlugin(ViewDumpPlugin viewDumpPlugin) {
    sPlugins.add(viewDumpPlugin);
  }

  /**
   * Removes a previously added plugin
   */
  public static void removePlugin(ViewDumpPlugin viewDumpPlugin) {
    sPlugins.remove(viewDumpPlugin);
  }

  /**
   * Get the list of plugins, for internal use.
   */
  public static List<ViewDumpPlugin> getPlugins() {
    return new ArrayList<>(sPlugins);
  }
}
