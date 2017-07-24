/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.app.UiAutomation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Provides a directory for an Album to store its screenshots in.
 */
class ScreenshotDirectories {
  private static final String[] REQUIRED_PERMISSIONS = new String[] {
      // Constants used to alleviate potential API level conflicts
      "android.permission.WRITE_EXTERNAL_STORAGE",
      "android.permission.READ_EXTERNAL_STORAGE"
  };

  private Context mContext;

  public ScreenshotDirectories(Context context) {
    mContext = context;
  }

  public File get(String type) {
    checkPermissions();
    return getSdcardDir(type);
  }

  private void checkPermissions() {
    for (String permission : REQUIRED_PERMISSIONS) {
      if (mContext.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
        continue;
      }
      if (Build.VERSION.SDK_INT < 23) {
        throw new RuntimeException("We need " + permission + " permission for screenshot tests");
      }
      Context targetContext = Registry.getRegistry().instrumentation.getTargetContext();
      grantPermission(targetContext, permission);
      grantPermission(mContext, permission);
    }
  }

  private void grantPermission(Context context, String permission) {
    if (Build.VERSION.SDK_INT < 23) {
      return;
    }
    UiAutomation automation = Registry.getRegistry().instrumentation.getUiAutomation();
    String command = String.format(
        Locale.ENGLISH,
        "pm grant %s %s",
        context.getPackageName(),
        permission);
    ParcelFileDescriptor pfd = automation.executeShellCommand(command);
    InputStream stream = new FileInputStream(pfd.getFileDescriptor());
    try {
      byte[] buffer = new byte[1024];
      while (stream.read(buffer) != -1) {
        // Consume stdout to ensure the command completes
      }
    } catch (IOException ignored) {
    } finally {
      try {
        stream.close();
      } catch (IOException ignored) {
      }
    }
  }

  private File getSdcardDir(String type) {
    String externalStorage = System.getenv("EXTERNAL_STORAGE");

    if (externalStorage == null) {
      throw new RuntimeException("No $EXTERNAL_STORAGE has been set on the device, please report this bug!");
    }

    String parent = String.format(
      "%s/screenshots/%s/",
      externalStorage,
      mContext.getPackageName());

    String child = String.format("%s/screenshots-%s", parent, type);

    new File(parent).mkdirs();

    File dir = new File(child);
    dir.mkdir();

    if (!dir.exists()) {
      throw new RuntimeException("Failed to create the directory for screenshots. Is your sdcard directory read-only?");
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
