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

package com.facebook.testing.screenshot.layouthierarchy.common;

import static org.junit.Assert.assertEquals;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests {@link TextViewAttributePlugin} */
@RunWith(AndroidJUnit4.class)
public class TextViewAttributePluginTest {
  @Test
  public void testAcceptsTextViews() {
    TextView textView = new TextView(InstrumentationRegistry.getTargetContext());
    assertEquals(true, TextViewAttributePlugin.getInstance().accept(textView));
  }

  @Test
  public void testDoesntAcceptOtherViews() {
    View view = new View(InstrumentationRegistry.getTargetContext());
    assertEquals(false, TextViewAttributePlugin.getInstance().accept(view));
  }

  @Test
  public void testPutsExpectedAttributes() throws Throwable {
    JSONObject node = new JSONObject();
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

    TextViewAttributePlugin.getInstance().putAttributes(node, textView, new Point());

    assertEquals("foobar", node.getString("TextView:text"));
    assertEquals("1337.0", node.getString("TextView:textSize"));
    assertEquals("ff000000", node.getString("TextView:textColor"));
    if (Build.VERSION.SDK_INT >= 17) {
      assertEquals("TEXT_ALIGNMENT_GRAVITY", node.getString("TextView:textAlignment"));
    }
  }

  @Test
  public void testNullDoesntKillUs() throws Throwable {
    JSONObject node = new JSONObject();
    TextView textView =
        new TextView(InstrumentationRegistry.getTargetContext()) {
          @Override
          public CharSequence getText() {
            return null;
          }
        };

    TextViewAttributePlugin.getInstance().putAttributes(node, textView, new Point());

    assertEquals("null", node.getString("TextView:text"));
  }

  @Test
  public void testABadTextViewDoesntKillUs() throws Throwable {
    JSONObject node = new JSONObject();
    TextView textView =
        new TextView(InstrumentationRegistry.getTargetContext()) {
          @Override
          public CharSequence getText() {
            throw new RuntimeException("Foobar");
          }
        };

    TextViewAttributePlugin.getInstance().putAttributes(node, textView, new Point());

    assertEquals("Foobar", node.getString("TextView:text"));
  }
}
