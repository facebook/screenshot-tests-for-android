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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** This exists to help alleviate some API transition pain */
public class OldApiBandaid {
  private OldApiBandaid() {}

  public static void assertMatchesRegex(String expected, String actual) {
    assertMatchesRegex(null, expected, actual);
  }

  public static void assertMatchesRegex(String message, String expected, String actual) {
    if (actual == null) {
      throw new IllegalStateException("Actual == null");
    }
    Pattern pattern = Pattern.compile(expected);
    Matcher matcher = pattern.matcher(actual);
    if (!matcher.matches()) {
      if (message != null) {
        throw new IllegalStateException(message + " " + actual);
      }
      throw new IllegalStateException(actual + " does not match regex " + expected);
    }
  }

  public static void assertContainsRegex(String expected, String actual) {
    if (actual == null) {
      throw new IllegalStateException("Actual == null");
    }
    Pattern pattern = Pattern.compile(expected);
    Matcher matcher = pattern.matcher(actual);
    if (!matcher.find()) {
      throw new IllegalStateException(actual + " does not contain regex " + expected);
    }
  }
}
