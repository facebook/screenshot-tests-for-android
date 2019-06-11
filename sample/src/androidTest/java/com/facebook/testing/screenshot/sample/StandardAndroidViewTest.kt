/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.sample

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import org.junit.Test

class StandardAndroidViewTest {
  @Test
  @Throws(Throwable::class)
  fun testRendering() {
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    val inflater = LayoutInflater.from(targetContext)
    val view = inflater.inflate(R.layout.search_bar, null, false)

    ViewHelpers.setupView(view).setExactWidthDp(300).layout()
    Screenshot.snap(view).record()
  }

  @Test
  @Throws(Throwable::class)
  fun testLongText() {
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    val inflater = LayoutInflater.from(targetContext)
    val view = inflater.inflate(R.layout.search_bar, null, false)

    val tv = view.findViewById<View>(R.id.search_box) as TextView

    tv.text = "This is a really long text and should overflow"
    ViewHelpers.setupView(view).setExactWidthDp(300).layout()

    Screenshot.snap(view).record()
  }

  @Test
  @Throws(Throwable::class)
  fun testChinese() {
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    val inflater = LayoutInflater.from(targetContext)
    val view = inflater.inflate(R.layout.search_bar, null, false)

    val tv = view.findViewById<View>(R.id.search_box) as TextView
    val btn = view.findViewById<View>(R.id.button) as TextView

    tv.hint = "搜索世界"
    btn.text = "搜"

    ViewHelpers.setupView(view).setExactWidthDp(300).layout()

    Screenshot.snap(view).record()
  }
}

