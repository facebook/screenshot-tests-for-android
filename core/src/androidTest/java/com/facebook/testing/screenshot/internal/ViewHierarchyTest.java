/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.testing.screenshot.ViewHelpers;
import com.facebook.testing.screenshot.plugin.PluginRegistry;
import com.facebook.testing.screenshot.plugin.ViewDumpPlugin;
import com.facebook.testing.screenshot.test.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ViewHierarchy}
 */
public class ViewHierarchyTest {
  private View mView;

  /**
   * Information about one View and all its children in the view
   * hierarchy.
   */
  public static class ParsedViewDetail {
    /**
     * The class name of the View, for instance
     * "android.widget.TextView"
     */
    private String mName;

    /**
     * The list of child views.
     */
    private final List<ParsedViewDetail> mChildren = new ArrayList<>();

    /**
     * The absolute coordinates of this view with respect to some top
     * level view that was originally dumped
     */
    private Rect mAbsoluteRect;

    public void setName(String name) {
      mName = name;
    }

    public String getName() {
      return mName;
    }

    public void addChild(ParsedViewDetail node) {
      mChildren.add(node);
    }

    public List<ParsedViewDetail> getChildren() {
      return mChildren;
    }

    public ParsedViewDetail getChild(int idx) {
      return mChildren.get(idx);
    }

    public void setAbsoluteRect(Rect absoluteRect) {
      mAbsoluteRect = absoluteRect;
    }

    public Rect getAbsoluteRect() {
      return mAbsoluteRect;
    }
  }

  private ParsedViewDetail convertToNode(JSONObject node) throws JSONException {
    ParsedViewDetail ret = new ParsedViewDetail();
    ret.setName(node.getString(ViewHierarchy.KEY_CLASS));
    ret.setAbsoluteRect(
        new Rect(
          node.getInt(ViewHierarchy.KEY_X),
          node.getInt(ViewHierarchy.KEY_Y),
          node.getInt(ViewHierarchy.KEY_X) + node.getInt(ViewHierarchy.KEY_WIDTH),
          node.getInt(ViewHierarchy.KEY_Y) + node.getInt(ViewHierarchy.KEY_HEIGHT)));

    JSONArray children = node.optJSONArray(ViewHierarchy.KEY_CHILDREN);
    if (children == null) {
      return ret;
    }
    for (int i = 0; i < children.length(); ++i) {
      ret.addChild(convertToNode(children.getJSONObject(i)));
    }

    return ret;
  }

  /**
   * Deflates a view into an XML.
   *
   * Just to be clear though, this is only for debugging and it won't
   * look anything like a real Android layout file.
   */
  private ParsedViewDetail deflate(View view) throws JSONException {
    return convertToNode(ViewHierarchy.dump(view));
  }

  @Before
  public void setUp() throws Exception {
    mView = LayoutInflater.from(InstrumentationRegistry.getTargetContext())
      .inflate(R.layout.testing_for_view_hierarchy, null, false);
  }

  @After
  public void tearDown() throws Exception {
    PluginRegistry.removePlugin(mDumpTextPlugin);
    PluginRegistry.removePlugin(mMyViewDumpPlugin);
  }

  @Test
  public void testClassNames() throws Throwable {
    ParsedViewDetail node = deflate(mView);
    assertEquals("android.widget.LinearLayout", node.getName());
    assertEquals("android.widget.TextView", node.getChild(2).getChild(0).getName());
  }

  @Test
  public void testBasicCoordinateCheck() throws Throwable {
    ViewHelpers.setupView(mView)
      .setExactHeightPx(1000)
      .setExactWidthPx(20000)
      .layout();
    ParsedViewDetail node = deflate(mView);
    assertEquals(0, node.getAbsoluteRect().top);
    assertEquals(0, node.getAbsoluteRect().left);

    assertTrue(node.getChild(0).getAbsoluteRect().bottom != 0);
    assertEquals(node.getChild(0).getAbsoluteRect().bottom,
                 node.getChild(1).getAbsoluteRect().top);
  }

  @Test
  public void testNestedAbsoluteCoordinates() throws Throwable {
    ViewHelpers.setupView(mView)
      .setExactHeightPx(1000)
      .setExactWidthPx(20000)
      .layout();
    ParsedViewDetail node = deflate(mView);

    int textViewHeight = ((ViewGroup) mView).getChildAt(0).getHeight();

    assertEquals(3 * textViewHeight,
                 node.getChild(2).getChild(1).getAbsoluteRect().top);
  }

  @Test
  public void testDumpHierarchyOnNestedNode() throws Throwable {
    ViewHelpers.setupView(mView)
      .setExactHeightPx(1000)
      .setExactWidthPx(20000)
      .layout();
    ParsedViewDetail node = deflate(((ViewGroup) mView).getChildAt(2));

    assertEquals(0, node.getAbsoluteRect().top);
    assertEquals(0, node.getAbsoluteRect().left);
    int textViewHeight = ((ViewGroup) mView).getChildAt(0).getHeight();

    assertEquals(textViewHeight,
                 node.getChild(1).getAbsoluteRect().top);
  }

  @Test
  public void testPluginDumps() throws Throwable {
    ViewHelpers.setupView(mView)
      .setExactHeightPx(1000)
      .setExactWidthPx(20000)
      .layout();
    PluginRegistry.addPlugin(mMyViewDumpPlugin);
    JSONObject root = ViewHierarchy.dump(mView);

    assertEquals("bar", root.getString("foo"));
  }

  @Test
  public void testPluginDumpsRecursively() throws Throwable {
    ViewHelpers.setupView(mView)
      .setExactHeightPx(1000)
      .setExactWidthPx(20000)
      .layout();
    PluginRegistry.addPlugin(mDumpTextPlugin);
    JSONObject node = ViewHierarchy.dump(mView);

    List<String> allText = new ArrayList<>();
    Queue<JSONObject> toCheck = new ArrayDeque<>();
    toCheck.offer(node);
    while (!toCheck.isEmpty()) {
      JSONObject object = toCheck.poll();
      String maybeText = object.optString("text");
      if (!TextUtils.isEmpty(maybeText)) {
        allText.add(maybeText);
      }
      JSONArray children = object.optJSONArray(ViewHierarchy.KEY_CHILDREN);
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

    assertEquals(expected, allText);
  }

  private ViewDumpPlugin mMyViewDumpPlugin = new ViewDumpPlugin() {
      public void dump(View view, Map<String, String> output) {
        output.put("foo", "bar");
      }
    };

  private ViewDumpPlugin mDumpTextPlugin = new ViewDumpPlugin() {
      public void dump(View view, Map<String, String> output) {
        if (view instanceof TextView) {
          output.put("text", ((TextView) view).getText().toString());
        }
      }
    };
}
