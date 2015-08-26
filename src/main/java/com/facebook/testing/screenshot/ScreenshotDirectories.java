/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

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
    File dir = mContext.getDir("screenshots-" + type, Context.MODE_WORLD_READABLE);

    // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's
    // manually set this
    dir.setWritable(/* writeable = */ true, /* ownerOnly = */ false);
    return dir;
  }
}
