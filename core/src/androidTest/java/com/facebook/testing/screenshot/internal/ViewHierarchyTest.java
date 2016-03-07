/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.graphics.Rect;
import android.test.InstrumentationTestCase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.testing.screenshot.ViewHelpers;
import com.facebook.testing.screenshot.plugin.PluginRegistry;
import com.facebook.testing.screenshot.plugin.ViewDumpPlugin;
import com.facebook.testing.screenshot.test.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Tests {@link ViewHierarchy}
 */
public class ViewHierarchyTest extends InstrumentationTestCase {
  private View mView;
  private ViewHierarchy mViewHierarchy;

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
    private final List<ParsedViewDetail> mChildren = new ArrayList<ParsedViewDetail>();

    /**
     * The absolute coordinates of this view with respect to some top
     * level view that was originally passed to {@link #deflate()}
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

  private ParsedViewDetail convertToNode(Element view) {
    ParsedViewDetail ret = new ParsedViewDetail();

    Rect rect = new Rect();
    for (int i = 0; i < view.getChildNodes().getLength(); i++) {
      Element c = (Element) view.getChildNodes().item(i);
      String name = c.getNodeName();

      if (name.equals("name")) {
        ret.setName(c.getTextContent());
      } else if (name.equals("left")) {
        rect.left = Integer.parseInt(c.getTextContent());
      } else if (name.equals("top")) {
        rect.top = Integer.parseInt(c.getTextContent());
      } else if (name.equals("right")) {
        rect.right = Integer.parseInt(c.getTextContent());
      } else if (name.equals("bottom")) {
        rect.bottom = Integer.parseInt(c.getTextContent());
      } else if (name.equals("children")) {
        NodeList childViews = c.getChildNodes();
        for (int j = 0; j < childViews.getLength(); j++) {
          ret.addChild(convertToNode((Element) childViews.item(j)));
        }
      }
    }

    ret.setAbsoluteRect(rect);

    return ret;
  }

  /**
   * Deflates a view into an XML.
   *
   * Just to be clear though, this is only for debugging and it won't
   * look anything like a real Android layout file.
   */
  private ParsedViewDetail deflate(View view) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      mViewHierarchy.deflate(view, os);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();

      Document doc = documentBuilder.parse(new ByteArrayInputStream(os.toByteArray()));
      Element root = (Element) doc.getFirstChild();
      return convertToNode(root);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    mViewHierarchy = new ViewHierarchy();
    mView = LayoutInflater.from(getInstrumentation().getTargetContext())
      .inflate(R.layout.testing_for_view_hierarchy, null, false);
  }

  @Override
  public void tearDown() throws Exception {
    PluginRegistry.removePlugin(mDumpTextPlugin);
    PluginRegistry.removePlugin(mMyViewDumpPlugin);
    super.tearDown();
  }

  public void testClassNames() throws Throwable {
    ParsedViewDetail node = deflate(mView);
    assertEquals("android.widget.LinearLayout", node.getName());
    assertEquals("android.widget.TextView", node.getChild(2).getChild(0).getName());
  }

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

  public void testPluginDumps() throws Throwable {
    ViewHelpers.setupView(mView)
      .setExactHeightPx(1000)
      .setExactWidthPx(20000)
      .layout();
    PluginRegistry.addPlugin(mMyViewDumpPlugin);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    mViewHierarchy.deflate(mView, os);


    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = factory.newDocumentBuilder();

    Document doc = documentBuilder.parse(new ByteArrayInputStream(os.toByteArray()));
    Element root = (Element) doc.getFirstChild();

    assertEquals("bar", getExtraValue(root, "foo"));
  }

  public void testPluginDumpsRecursively() throws Throwable {
    ViewHelpers.setupView(mView)
      .setExactHeightPx(1000)
      .setExactWidthPx(20000)
      .layout();
    PluginRegistry.addPlugin(mDumpTextPlugin);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    mViewHierarchy.deflate(mView, os);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = factory.newDocumentBuilder();

    Document doc = documentBuilder.parse(new ByteArrayInputStream(os.toByteArray()));
    Element root = (Element) doc.getFirstChild();

    List<String> allText = new ArrayList<>();
    getAllText(root, allText);
    List<String> expected = new ArrayList<>();
    expected.add("foobar");
    expected.add("foobar2");
    expected.add("foobar3");
    expected.add("foobar4");

    assertEquals(expected, allText);
  }

  private void getAllText(Element el, List<String> output) {
    String text = getExtraValue(el, "text");
    if (text != null) {
      output.add(text);
    }

    NodeList children = el.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Element e = (Element) children.item(i);
      if (e.getNodeName().equals("children")) {
        NodeList actualChildren = e.getChildNodes();
        for (int j = 0; j < actualChildren.getLength(); j++) {
          Element childEl = (Element) actualChildren.item(j);
          getAllText(childEl, output);
        }
      }
    }
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

  private String getExtraValue(Element parent, String tagName) {
    NodeList nodeList = parent.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Element el = (Element)nodeList.item(i);
      if (el.getAttribute("key").equals(tagName)) {
        return el.getTextContent();
      }
    }

    return null;
  }
}
