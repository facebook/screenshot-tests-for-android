/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.example.litho

import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.widget.Image
import com.facebook.yoga.YogaEdge

@LayoutSpec
object ImageRowSpec {
  @OnCreateLayout
  fun onCreateLayout(c: ComponentContext): ComponentLayout =
      Row.create(c)
          .child(
              Image.create(c)
                  .drawableRes(R.drawable.ic_launcher_background)
                  .widthDip(64f)
                  .heightDip(64f)
                  .paddingDip(YogaEdge.ALL, 4f))
          .child(
              Image.create(c)
                  .drawableRes(R.drawable.ic_launcher_background)
                  .widthDip(128f)
                  .heightDip(128f)
                  .paddingDip(YogaEdge.ALL, 4f))
          .child(
              Image.create(c)
                  .drawableRes(R.drawable.ic_launcher_background)
                  .widthDip(32f)
                  .heightDip(32f)
                  .paddingDip(YogaEdge.ALL, 4f))
          .build()
}