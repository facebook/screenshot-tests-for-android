/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.testing.screenshot.internal;

import static com.facebook.testing.screenshot.ScreenshotRunner.SDCARD_DIRECTORY;

import android.annotation.SuppressLint;
import android.app.UiAutomation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/** Provides a directory for an Album to store its screenshots in. */
class ScreenshotDirectories {
  // Constants used to alleviate potential API level conflicts
  private static final String WRITE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
  private static final String READ_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE";
  private static final String[] REQUIRED_PERMISSIONS =
      new String[] {WRITE_PERMISSION, READ_PERMISSION};

  private Context mContext;
  private Bundle mArguments;

  private static final String DEFAULT_SDCARD_DIRECTORY = "screenshots";

  public ScreenshotDirectories(Context context) {
    mContext = context;
    mArguments = Registry.getRegistry().arguments;
  }

  public File get(String type) {
    checkPermissions();
    return getSdcardDir(type);
  }

  private void checkPermissions() {
    for (String permission : REQUIRED_PERMISSIONS) {
      if ((permission.equals(READ_PERMISSION) && Build.VERSION.SDK_INT < 16)
          || mContext.checkCallingOrSelfPermission(permission)
              == PackageManager.PERMISSION_GRANTED) {
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
    String command =
        String.format(Locale.ENGLISH, "pm grant %s %s", context.getPackageName(), permission);
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
      try {
        pfd.close();
      } catch (IOException ignored) {
      }
    }
  }

  private File getSdcardDir(String type) {
    String externalStorage = System.getenv("EXTERNAL_STORAGE");

    if (externalStorage == null) {
      throw new RuntimeException(
          "No $EXTERNAL_STORAGE has been set on the device, please report this bug!");
    }

    String sdcardDirectory =
        mArguments.containsKey(SDCARD_DIRECTORY)
            ? mArguments.getString(SDCARD_DIRECTORY)
            : DEFAULT_SDCARD_DIRECTORY;

    String parent =
        String.format("%s/%s/%s/", externalStorage, sdcardDirectory, mContext.getPackageName());

    String child = String.format("%s/screenshots-%s", parent, type);

    new File(parent).mkdirs();

    File dir = new File(child);
    dir.mkdir();

    if (!dir.exists()) {
      throw new RuntimeException(
          "Failed to create the directory "
              + dir.getAbsolutePath()
              + " for screenshots. Is your sdcard directory read-only?");
    }

    setWorldWriteable(dir);
    return dir;
  }

  @SuppressLint("SetWorldWritable")
  private void setWorldWriteable(File dir) {
    // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's
    // manually set this
    dir.setWritable(/* writeable = */ true, /* ownerOnly = */ false);
  }
}
