/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot;

/**
 * Builds all the information related to a screenshot.
 */
public interface RecordBuilder {
  /**
   * Set a name (identifier) for the screenshot. If you skip the name
   * a name will be generated based on the Test class and Test method
   * name this is being run from. That means if you have multiple
   * screenshots in the same test, then you have to explicitly specify
   * names to disambiguate.
   */
  RecordBuilder setName(String name);

  /**
   * Set device identifier for the screenshot. If you skip the device
   * identifier a device identifier will be generated based on the
   * device android API version, presence of Google Play Services
   * and screen size. An example could be:
   * API_25_GP_1920_1080_com.example.screenshots.MainActivityTest_mainActivityTestSettingsOpen
   * If you not want to specify a device identifier, just set it to null
   * or an implementation of DeviceIdentifier which returns always an empty string.
   */
  RecordBuilder setDeviceIdentifier(DeviceIdentifier deviceIdentifier);

  /**
   * Set a long description of the what the screenshot is about.
   *
   * This will be shown as part of the report, and in general it can
   * help document a screenshot if you're using it as part of an
   * external tooling.
   */
  RecordBuilder setDescription(String description);

  /**
   * Add extra metadata about this screenshots.
   *
   * There will be no semantic information associated with this
   * metadata, but we'll try to provide this as debugging information
   * whenever you're viewing screenshots.
   */
  RecordBuilder addExtra(String key, String value);

  /**
   * Groups similar or identical screenshots which makes it easier to
   * compare.
   */
  RecordBuilder setGroup(String groupName);

  /**
   * Finish the recording.
   */
  void record();
}
