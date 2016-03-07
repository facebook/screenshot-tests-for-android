// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot.plugin;

import java.util.Map;

import android.view.View;

/**
 * A plugin to get more metadata about a View.
 *
 * When screenshots are generated we use all registered plugins to
 * generate metadata for each of the views in the hierarchy.
 */
public interface ViewDumpPlugin {
  public void dump(View view, Map<String, String> output);
}
