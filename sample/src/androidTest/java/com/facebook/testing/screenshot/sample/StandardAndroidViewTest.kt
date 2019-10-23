/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

