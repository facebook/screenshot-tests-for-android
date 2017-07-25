/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.graphics.Bitmap;
import android.view.View;

import com.facebook.testing.screenshot.RecordBuilder;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * A builder for all the metadata associated with a screenshot.
 *
 * Use Screenshot#snap() or Screenshot#snapActivity() to get an
 * instance of this, and commit the record with #record().
 */
public class RecordBuilderImpl implements RecordBuilder {
  private final ScreenshotImpl mScreenshotImpl;

  private String mDescription;
  private String mName;
  private String mTestClass;
  private String mTestName;
  private String mError;
  private String mGroup;

  private Tiling mTiling = new Tiling(1, 1);
  private View mView;
  private final Map<String, String> mExtras = new HashMap<String, String>();

  /* package */ RecordBuilderImpl(ScreenshotImpl screenshotImpl) {
    mScreenshotImpl = screenshotImpl;
  }

  /**
   * @inherit
   */
  @Override
  public RecordBuilderImpl setDescription(String description) {
    mDescription = description;
    return this;
  }

  public String getDescription() {
    return mDescription;
  }

  /**
   * @inherit
   */
  @Override
  public RecordBuilderImpl setName(String name) {
    CharsetEncoder charsetEncoder = Charset.forName("latin-1").newEncoder();

    if (!charsetEncoder.canEncode(name)) {
      throw new IllegalArgumentException(
          "Screenshot names must have only latin characters: " + name);
    }
    if (name.contains(File.separator)) {
      throw new IllegalArgumentException(
          "Screenshot names cannot contain '" + File.separator + "': " + name);
    }

    mName = name;
    return this;
  }

  public String getName() {
    if (mName == null) {
      return getTestClass() + "_" + getTestName();
    }
    return mName;
  }

  /**
   * Set the name of the test from which this screenshot is
   * generated. This should be detected by default most of the time.
   */
  public RecordBuilderImpl setTestName(String testName) {
    mTestName = testName;
    return this;
  }

  public String getTestName() {
    return mTestName;
  }

  /**
   * Set the class name of the TestCase from which this screenshot is
   * generated. This should be detected by default most of the time.
   */
  public RecordBuilderImpl setTestClass(String testClass) {
    mTestClass = testClass;
    return this;
  }

  public String getTestClass() {
    return mTestClass;
  }

  /**
   * Stops the recording and returns the generated bitmap, possibly
   * compressed.
   *
   * You cannot call this after record(), nor can you call record()
   * after this call.
   */
  public Bitmap getBitmap() {
    return mScreenshotImpl.getBitmap(this);
  }

  /**
   * Returns true if this record has been given an explicit name using
   * setName(). If false, getName() will still generate a name.
   */
  public boolean hasExplicitName() {
    return mName != null;
  }

  /* package */ RecordBuilderImpl setError(String error) {
    mError = error;
    return this;
  }

  /**
   * Get's any error that was encountered while creating the
   * screenshot.
   */
  public String getError() {
    return mError;
  }

  /**
   * @inherit
   */
  @Override
  public void record() {
    mScreenshotImpl.record(this);
    checkState();
  }

  @Override
  public RecordBuilderImpl setGroup(String groupName) {
    mGroup = groupName;
    return this;
  }

  /**
   * Sanity checks that the record is ready to be persisted
   */
  /* package */ void checkState() {
    if (mError != null) {
      return;
    }
    for (int i = 0; i < mTiling.getWidth(); i++) {
      for (int j = 0; j < mTiling.getHeight(); j++) {
        if (mTiling.getAt(i, j) == null) {
          throw new IllegalStateException("expected all tiles to be filled");
        }
      }
    }
  }

  /* package */ RecordBuilderImpl setView(View view) {
    mView = view;
    return this;
  }

  public View getView() {
    return mView;
  }

  /* package */ RecordBuilderImpl setTiling(Tiling tiling) {
    mTiling = tiling;
    return this;
  }

  public Tiling getTiling() {
    return mTiling;
  }

  @Override
  public RecordBuilderImpl addExtra(String key, String value) {
    mExtras.put(key, value);
    return this;
  }

  public Map<String, String> getExtras() {
    return mExtras;
  }

  public String getGroup() {
    return mGroup;
  }
}
