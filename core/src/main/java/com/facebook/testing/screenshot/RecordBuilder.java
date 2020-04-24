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

package com.facebook.testing.screenshot;

import android.graphics.Bitmap;

/** Builds all the information related to a screenshot. */
public interface RecordBuilder {
  /**
   * Set a name (identifier) for the screenshot. If you skip the name a name will be generated based
   * on the Test class and Test method name this is being run from. That means if you have multiple
   * screenshots in the same test, then you have to explicitly specify names to disambiguate.
   */
  RecordBuilder setName(String name);

  /**
   * Set a long description of the what the screenshot is about.
   *
   * <p>This will be shown as part of the report, and in general it can help document a screenshot
   * if you're using it as part of an external tooling.
   */
  RecordBuilder setDescription(String description);

  /**
   * Add extra metadata about this screenshots.
   *
   * <p>There will be no semantic information associated with this metadata, but we'll try to
   * provide this as debugging information whenever you're viewing screenshots.
   */
  RecordBuilder addExtra(String key, String value);

  /** Groups similar or identical screenshots which makes it easier to compare. */
  RecordBuilder setGroup(String groupName);

  /**
   * Enables or disables extra information attached to the metadata generated related to
   * accessibility information.
   *
   * @param includeAccessibilityInfo
   */
  RecordBuilder setIncludeAccessibilityInfo(boolean includeAccessibilityInfo);

  /**
   * Stops the recording and returns the generated bitmap, possibly compressed.
   *
   * <p>You cannot call this after record(), nor can you call record() after this call.
   */
  Bitmap getBitmap();

  /**
   * Set the maximum number of pixels this screenshot should produce. Producing any number higher
   * will throw an exception.
   *
   * @param maxPixels Maximum number of pixels this screenshot should produce. <= 0 for no limit.
   */
  public RecordBuilder setMaxPixels(long maxPixels);

  /** Finish the recording. */
  void record();
}
