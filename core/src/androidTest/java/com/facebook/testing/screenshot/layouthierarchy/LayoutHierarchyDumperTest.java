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

package com.facebook.testing.screenshot.layouthierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import com.facebook.testing.screenshot.ViewHelpers;
import com.facebook.testing.screenshot.test.R;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests {@link LayoutHierarchyDumper} */
@RunWith(AndroidJUnit4.class)
public class LayoutHierarchyDumperTest {
  private View mView;

  private final AttributePlugin mAttributePlugin =
      new AbstractAttributePlugin() {
        @Override
        public boolean accept(Object obj) {
          return true;
        }

        @Override
        public String namespace() {
          return "foo";
        }

        @Override
        public void putAttributes(JSONObject node, Object obj, Point offset) throws JSONException {
          put(node, "foo", "bar");
        }
      };

  private final AttributePlugin mTextAttributePlugin =
      new AbstractAttributePlugin() {
        @Override
        public boolean accept(Object obj) {
          return obj instanceof TextView;
        }

        @Override
        public String namespace() {
          return "Text";
        }

        @Override
        public void putAttributes(JSONObject node, Object obj, Point offset) throws JSONException {
          put(node, "text", ((TextView) obj).getText().toString());
        }
      };

  /** Utility class to make inspecting a serialized hierarchy easier */
  static class ParsedViewDetail {
    final List<ParsedViewDetail> children = new ArrayList<>();

    final String name;
    final Rect absoluteRect;

    ParsedViewDetail(String name, Rect absoluteRect) {
      this.name = name;
      this.absoluteRect = absoluteRect;
    }

    ParsedViewDetail childAt(int index) {
      return children.get(index);
    }

    static ParsedViewDetail convert(View view) throws JSONException {
      return convert(LayoutHierarchyDumper.create().dumpHierarchy(view));
    }

    static ParsedViewDetail convert(JSONObject node) throws JSONException {
      final int left = node.getInt(BaseViewAttributePlugin.KEY_LEFT);
      final int top = node.getInt(BaseViewAttributePlugin.KEY_TOP);
      ParsedViewDetail detail =
          new ParsedViewDetail(
              node.getString(BaseViewAttributePlugin.KEY_CLASS),
              new Rect(
                  left,
                  top,
                  left + node.getInt(BaseViewAttributePlugin.KEY_WIDTH),
                  top + node.getInt(BaseViewAttributePlugin.KEY_HEIGHT)));

      JSONArray children = node.optJSONArray(BaseViewHierarchyPlugin.KEY_CHILDREN);
      if (children == null) {
        return detail;
      }

      for (int i = 0; i < children.length(); ++i) {
        detail.children.add(convert(children.getJSONObject(i)));
      }

      return detail;
    }
  }

  @Before
  public void setUp() throws Exception {
    mView =
        LayoutInflater.from(InstrumentationRegistry.getTargetContext())
            .inflate(R.layout.testing_for_view_hierarchy, null, false);
  }

  @Test
  public void testClassNames() throws Throwable {
    ParsedViewDetail node = ParsedViewDetail.convert(mView);
    assertThat(node.name).isEqualTo("android.widget.LinearLayout");
    assertThat(node.childAt(2).childAt(0).name).isEqualTo("android.widget.TextView");
  }

  @Test
  public void testBasicCoordinateCheck() throws Throwable {
    ViewHelpers.setupView(mView).setExactHeightPx(1000).setExactWidthPx(20000).layout();
    ParsedViewDetail node = ParsedViewDetail.convert(mView);
    assertThat(node.absoluteRect.top).isEqualTo(0);
    assertThat(node.absoluteRect.left).isEqualTo(0);
    assertThat(node.childAt(1).absoluteRect.top).isEqualTo(node.childAt(0).absoluteRect.bottom);
    assertThat(node.childAt(0).absoluteRect.bottom != 0).isTrue();
  }

  @Test
  public void testNestedAbsoluteCoordinates() throws Throwable {
    ViewHelpers.setupView(mView).setExactHeightPx(1000).setExactWidthPx(20000).layout();
    ParsedViewDetail node = ParsedViewDetail.convert(mView);

    int textViewHeight = ((ViewGroup) mView).getChildAt(0).getHeight();

    assertThat(node.childAt(2).childAt(1).absoluteRect.top).isEqualTo(3 * textViewHeight);
  }

  @Test
  public void testDumpHierarchyOnNestedNode() throws Throwable {
    ViewHelpers.setupView(mView).setExactHeightPx(1000).setExactWidthPx(20000).layout();
    ParsedViewDetail node = ParsedViewDetail.convert(((ViewGroup) mView).getChildAt(2));

    assertThat(node.absoluteRect.top).isEqualTo(0);
    assertThat(node.absoluteRect.left).isEqualTo(0);
    int textViewHeight = ((ViewGroup) mView).getChildAt(0).getHeight();

    assertThat(node.childAt(1).absoluteRect.top).isEqualTo(textViewHeight);
  }

  @Test
  public void testPluginDumps() throws Throwable {
    ViewHelpers.setupView(mView).setExactHeightPx(1000).setExactWidthPx(20000).layout();

    LayoutHierarchyDumper dumper =
        LayoutHierarchyDumper.createWith(
            Collections.<HierarchyPlugin>emptyList(), Collections.singletonList(mAttributePlugin));
    JSONObject root = dumper.dumpHierarchy(mView);

    assertThat(root.getString("foo:foo")).isEqualTo("bar");
  }

  @Test
  public void testPluginDumpsRecursively() throws Throwable {
    ViewHelpers.setupView(mView).setExactHeightPx(1000).setExactWidthPx(20000).layout();

    LayoutHierarchyDumper dumper =
        LayoutHierarchyDumper.createWith(
            Collections.<HierarchyPlugin>emptyList(),
            Collections.singletonList(mTextAttributePlugin));

    JSONObject node = dumper.dumpHierarchy(mView);

    List<String> allText = new ArrayList<>();
    Queue<JSONObject> toCheck = new ArrayDeque<>();
    toCheck.offer(node);
    while (!toCheck.isEmpty()) {
      JSONObject object = toCheck.poll();
      String maybeText = object.optString("Text:text");
      if (!TextUtils.isEmpty(maybeText)) {
        allText.add(maybeText);
      }
      JSONArray children = object.optJSONArray(BaseViewHierarchyPlugin.KEY_CHILDREN);
      if (children == null) {
        continue;
      }
      for (int i = 0; i < children.length(); ++i) {
        toCheck.offer(children.getJSONObject(i));
      }
    }

    List<String> expected = new ArrayList<>();
    expected.add("foobar");
    expected.add("foobar2");
    expected.add("foobar3");
    expected.add("foobar4");

    assertThat(allText).isEqualTo(expected);
  }
}
