/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
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
