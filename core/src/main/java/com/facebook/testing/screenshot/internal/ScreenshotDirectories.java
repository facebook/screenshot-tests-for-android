/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import java.io.File;

import android.content.Context;

/**
 * Provides a directory for an Album to store its screenshots in.
 */
class ScreenshotDirectories {
  private Context mContext;

  public ScreenshotDirectories(Context context) {
    mContext = context;
  }

  public File get(String type) {
    return getSdcardDir(type);
  }

  private File getSdcardDir(String type) {

    String parent = String.format(
      "/sdcard/screenshots/%s/",
      mContext.getPackageName());

    String child = String.format("%s/screenshots-%s", parent, type);

    new File(parent).mkdirs();

    File dir = new File(child);
    dir.mkdir();

    setWorldWriteable(dir);
    return dir;
  }

  private File getDataDir(String type) {
    File dir = mContext.getDir("screenshots-" + type, Context.MODE_WORLD_READABLE);

    setWorldWriteable(dir);
    return dir;
  }

  private void setWorldWriteable(File dir) {
    // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's
    // manually set this
    dir.setWritable(/* writeable = */ true, /* ownerOnly = */ false);
  }
}
