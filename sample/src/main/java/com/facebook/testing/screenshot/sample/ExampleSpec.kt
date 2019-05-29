/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.sample

import com.facebook.litho.Border
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge

@LayoutSpec
object ExampleSpec {
  @OnCreateLayout
  fun onCreateLayout(c: ComponentContext): Component =
      Column.create(c)
          .child(ImageRow.create(c))
          .child(
              Text.create(c)
                  .textRes(R.string.large_text))
          .paddingDip(YogaEdge.ALL, 16f)
          .border(
              Border.create(c)
                  .colorRes(YogaEdge.ALL, R.color.colorPrimary)
                  .widthDip(YogaEdge.ALL, 8f)
                  .build())
          .build()
}
