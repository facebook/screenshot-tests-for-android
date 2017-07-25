/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.plugin;

import android.support.test.InstrumentationRegistry;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Dumps useful details from a TextView
 */
public class TextViewDumperTest {
  TextViewDumper mTextViewDumper;
  Map<String, String> mOutput = new HashMap<String, String>();
  TextView tv;

  @Before
  public void before() throws Throwable {
    mTextViewDumper = new TextViewDumper();
    tv = new TextView(InstrumentationRegistry.getTargetContext());
  }

  @Test
  public void testDumpsCorrectly() throws Throwable {
    tv.setText("foobar");

    mTextViewDumper.dump(tv, mOutput);
    assertEquals("foobar", mOutput.get("text"));
  }

  @Test
  public void testNullDoesntKillUs() throws Throwable {
    tv.setText(null);
    mTextViewDumper.dump(tv, mOutput);
    assertEquals("", mOutput.get("text"));
  }

  @Test
  public void testABadTextViewDoesntKillUs() throws Throwable {
    // Android engineers like to break the world
    tv = new TextView(InstrumentationRegistry.getTargetContext()) {
      @Override
      public CharSequence getText() {
        throw new RuntimeException("Foobar");
      }
    };

    tv.setText("bleh");
    mTextViewDumper.dump(tv, mOutput);

    assertEquals("unsupported", mOutput.get("text"));
  }
}
