/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.example.litho

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.facebook.litho.LithoView

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    findViewById<LithoView>(R.id.litho_view).apply {
      setComponent(Example.create(componentContext).build())
    }
  }
}
