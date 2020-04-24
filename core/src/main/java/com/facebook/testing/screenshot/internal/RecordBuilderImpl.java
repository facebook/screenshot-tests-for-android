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
 * <p>Use Screenshot#snap() or Screenshot#snapActivity() to get an instance of this, and commit the
 * record with #record().
 */
public class RecordBuilderImpl implements RecordBuilder {
  public static final long DEFAULT_MAX_PIXELS = 10000000L;
  private final ScreenshotImpl mScreenshotImpl;
  private final Map<String, String> mExtras = new HashMap<>();
  private String mDescription;
  private String mName;
  private String mTestClass;
  private String mTestName;
  private String mError;
  private String mGroup;
  private boolean mIncludeAccessibilityInfo = true;
  private Tiling mTiling = new Tiling(1, 1);
  private View mView;
  private long mMaxPixels = DEFAULT_MAX_PIXELS;

  /* package */ RecordBuilderImpl(ScreenshotImpl screenshotImpl) {
    mScreenshotImpl = screenshotImpl;
  }

  public String getDescription() {
    return mDescription;
  }

  /** @inherit */
  @Override
  public RecordBuilderImpl setDescription(String description) {
    mDescription = description;
    return this;
  }

  public String getName() {
    if (mName == null) {
      return getTestClass() + "_" + getTestName();
    }
    return mName;
  }

  /** @inherit */
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

  public String getTestName() {
    return mTestName;
  }

  /**
   * Set the name of the test from which this screenshot is generated. This should be detected by
   * default most of the time.
   */
  public RecordBuilderImpl setTestName(String testName) {
    mTestName = testName;
    return this;
  }

  public String getTestClass() {
    return mTestClass;
  }

  /**
   * Set the class name of the TestCase from which this screenshot is generated. This should be
   * detected by default most of the time.
   */
  public RecordBuilderImpl setTestClass(String testClass) {
    mTestClass = testClass;
    return this;
  }

  /** @inherit */
  @Override
  public Bitmap getBitmap() {
    return mScreenshotImpl.getBitmap(this);
  }

  /** @inherit */
  @Override
  public RecordBuilderImpl setMaxPixels(long maxPixels) {
    mMaxPixels = maxPixels;
    return this;
  }

  /** @return The maximum number of pixels that is expected to be produced by this screenshot */
  public long getMaxPixels() {
    return mMaxPixels;
  }

  /**
   * Returns true if this record has been given an explicit name using setName(). If false,
   * getName() will still generate a name.
   */
  public boolean hasExplicitName() {
    return mName != null;
  }

  /** Get's any error that was encountered while creating the screenshot. */
  public String getError() {
    return mError;
  }

  RecordBuilderImpl setError(String error) {
    mError = error;
    return this;
  }

  /** @inherit */
  @Override
  public void record() {
    mScreenshotImpl.record(this);
    checkState();
  }

  /** Sanity checks that the record is ready to be persisted */
  void checkState() {
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

  public View getView() {
    return mView;
  }

  RecordBuilderImpl setView(View view) {
    mView = view;
    return this;
  }

  public Tiling getTiling() {
    return mTiling;
  }

  RecordBuilderImpl setTiling(Tiling tiling) {
    mTiling = tiling;
    return this;
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

  @Override
  public RecordBuilderImpl setGroup(String groupName) {
    mGroup = groupName;
    return this;
  }

  /** @inherit */
  @Override
  public RecordBuilderImpl setIncludeAccessibilityInfo(boolean includeAccessibilityInfo) {
    mIncludeAccessibilityInfo = includeAccessibilityInfo;
    return this;
  }

  public boolean getIncludeAccessibilityInfo() {
    return mIncludeAccessibilityInfo;
  }
}
