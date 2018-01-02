/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.example.litho

import android.app.Application
import com.facebook.soloader.SoLoader

class App : Application() {
  override fun onCreate() {
    super.onCreate()
    SoLoader.init(this, false)
  }
}