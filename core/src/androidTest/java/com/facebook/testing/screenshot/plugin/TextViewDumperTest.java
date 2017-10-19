/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.plugin;

import android.graphics.Color;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Dumps useful details from a TextView */
public class TextViewDumperTest {
  @Test
  public void testDumpsCorrectly() throws Throwable {
    TextView textView =
        new TextView(InstrumentationRegistry.getTargetContext()) {
          @Override
          public CharSequence getText() {
            return "foobar";
          }

          @Override
          public float getTextSize() {
            return 1337f;
          }
        };
    textView.setTextColor(Color.BLACK);
    Map<String, String> output = new HashMap<>();

    TextViewDumper dumper = TextViewDumper.getInstance();
    dumper.dump(textView, output);

    ViewDumpAssertions.assertContainsPairs(
        dumper,
        output,
        TextViewDumper.TEXT,
        "foobar",
        TextViewDumper.TEXT_SIZE,
        "1337.0",
        TextViewDumper.TEXT_COLOR,
        "ff000000");

    if (Build.VERSION.SDK_INT >= 17) {
      ViewDumpAssertions.assertContainsPairs(
          dumper, output, TextViewDumper.TEXT_ALIGNMENT, "TEXT_ALIGNMENT_GRAVITY");
    }
  }

  @Test
  public void testNullDoesntKillUs() throws Throwable {
    TextView textView =
        new TextView(InstrumentationRegistry.getTargetContext()) {
          @Override
          public CharSequence getText() {
            return null;
          }
        };
    Map<String, String> output = new HashMap<>();

    TextViewDumper dumper = TextViewDumper.getInstance();
    dumper.dump(textView, output);

    ViewDumpAssertions.assertContainsPairs(dumper, output, TextViewDumper.TEXT, "null");
  }

  @Test
  public void testABadTextViewDoesntKillUs() throws Throwable {
    TextView textView =
        new TextView(InstrumentationRegistry.getTargetContext()) {
          @Override
          public CharSequence getText() {
            throw new RuntimeException("Foobar");
          }
        };
    Map<String, String> output = new HashMap<>();

    TextViewDumper dumper = TextViewDumper.getInstance();
    dumper.dump(textView, output);

    ViewDumpAssertions.assertContainsPairs(dumper, output, TextViewDumper.TEXT, "Foobar");
  }
}
