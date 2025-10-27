/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
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

import static org.assertj.core.api.Assertions.assertThat;

import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.widget.TextView;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests {@link TextViewAttributePlugin} */
@RunWith(AndroidJUnit4.class)
public class TextViewAttributePluginTest {
  @Test
  public void testAcceptsTextViews() {
    TextView textView = new TextView(InstrumentationRegistry.getTargetContext());
    assertThat(TextViewAttributePlugin.getInstance().accept(textView)).isTrue();
  }

  @Test
  public void testDoesntAcceptOtherViews() {
    View view = new View(InstrumentationRegistry.getTargetContext());
    assertThat(TextViewAttributePlugin.getInstance().accept(view)).isFalse();
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

    assertThat(node.getString("TextView:text")).isEqualTo("foobar");
    assertThat(node.getString("TextView:textSize")).isEqualTo("1337.0");
    assertThat(node.getString("TextView:textColor")).isEqualTo("ff000000");
    assertThat(node.getString("TextView:textAlignment")).isEqualTo("TEXT_ALIGNMENT_GRAVITY");
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

    assertThat(node.getString("TextView:text")).isEqualTo("null");
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

    assertThat(node.getString("TextView:text")).isEqualTo("Foobar");
  }
}
