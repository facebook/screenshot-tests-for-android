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

package com.facebook.testing.screenshot.internal;

/**
 * A 2D layout of image tiles. We represent images as strings which can be looked up in an {@code
 * AlbumImpl}
 */
public class Tiling {
  private int mWidth;
  private int mHeight;
  private String[][] mContents;

  public Tiling(int width, int height) {
    mWidth = width;
    mHeight = height;
    mContents = new String[width][height];
  }

  /** Convenience factory method for tests */
  public static Tiling singleTile(String name) {
    Tiling ret = new Tiling(1, 1);
    ret.setAt(0, 0, name);
    return ret;
  }

  public int getHeight() {
    return mHeight;
  }

  public int getWidth() {
    return mWidth;
  }

  public String getAt(int x, int y) {
    return mContents[x][y];
  }

  public void setAt(int x, int y, String name) {
    mContents[x][y] = name;
  }
}
