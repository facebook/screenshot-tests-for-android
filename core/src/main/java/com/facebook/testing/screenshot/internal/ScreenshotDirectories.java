/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * Provides a directory for an Album to store its screenshots in.
 */
class ScreenshotDirectories {
  private Context mContext;

  public ScreenshotDirectories(Context context) {
    mContext = context;
  }

  public File get(String type) {
    checkPermissions();
    return getSdcardDir(type);
  }

  private void checkPermissions() {
    int res = mContext.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
    if (res != PackageManager.PERMISSION_GRANTED) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        throw new RuntimeException("This does not currently work on API 23+, see "
            + "https://github.com/facebook/screenshot-tests-for-android/issues/16 for details.");
      } else {
        throw new RuntimeException("We need WRITE_EXTERNAL_STORAGE permission for screenshot tests");
      }
    }
  }

  private File getSdcardDir(String type) {

    String parent = String.format(
      "/sdcard/screenshots/%s/",
      mContext.getPackageName());

    String child = String.format("%s/screenshots-%s", parent, type);

    new File(parent).mkdirs();

    File dir = new File(child);
    dir.mkdir();

    if (!dir.exists()) {
      throw new RuntimeException("Failed to create the directory for screenshots, do you have WRITE_EXTERNAL_STORAGE permission?");
    }

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
