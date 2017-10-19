// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot.plugin;

import static org.junit.Assert.assertEquals;

import java.util.Map;

public class ViewDumpAssertions {
  private ViewDumpAssertions() {
    throw new AssertionError("No instances.");
  }

  public static void assertContainsPairs(
      ViewDumpPlugin plugin, Map<String, String> output, String... pairs) {
    if (pairs.length % 2 != 0) {
      throw new AssertionError("Given incomplete list of pairs");
    }

    for (int i = 0, size = pairs.length; i < size; i += 2) {
      String key = pairs[i];
      String value = pairs[i + 1];
      assertEquals(value, output.get(plugin.prefix(key)));
    }
  }
}
