/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.testing.screenshot.plugin.PluginRegistry;
import com.facebook.testing.screenshot.plugin.ViewDumpPlugin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Dumps information about the view hierarchy.
 */
public class ViewHierarchy {
  /**
   * Creates an XML dump for the view into given OutputStream
   * <p>
   * This is meant for debugging purposes only, and we don't
   * guarantee that it's format will remain the same.
   */
  public void deflate(View view, OutputStream out) throws IOException {
    Document doc = deflateToDocument(view);
    try {
      DOMSource source = new DOMSource(doc);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      StreamResult result = new StreamResult(out);
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(source, result);
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
  }

  private Document deflateToDocument(View view) {
    Document doc;

    try {
      doc = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .newDocument();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }

    doc.appendChild(deflateRelative(view, new Point(-view.getLeft(), -view.getTop()), doc));
    return doc;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private Element deflateRelative(View view, Point topLeft, Document doc) {
    Element el = doc.createElement("view");

    addTextNode(el, "name", view.getClass().getName());

    Rect rect = new Rect(
      topLeft.x + view.getLeft(),
      topLeft.y + view.getTop(),
      topLeft.x + view.getRight(),
      topLeft.y + view.getBottom());

    addTextNode(el, "left", String.valueOf(rect.left));
    addTextNode(el, "top", String.valueOf(rect.top));
    addTextNode(el, "right", String.valueOf(rect.right));
    addTextNode(el, "bottom", String.valueOf(rect.bottom));
    if (Build.VERSION.SDK_INT >= 11) {
      addTextNode(el, "isLayoutRequested", String.valueOf(view.isLayoutRequested()));
    }
    addTextNode(el, "isDirty", String.valueOf(view.isDirty()));

    Map<String, String> extraValues = new HashMap<>();
    for (ViewDumpPlugin plugin : PluginRegistry.getPlugins()) {
      plugin.dump(view, extraValues);
    }

    for (Map.Entry<String, String> extraValue : extraValues.entrySet()) {
      addExtraValue(el, extraValue.getKey(), extraValue.getValue());
    }

    Element children = doc.createElement("children");
    el.appendChild(children);

    if (view instanceof ViewGroup) {
      Point myTopLeft = new Point(rect.left, rect.top);
      ViewGroup vg = (ViewGroup) view;
      for (int i = 0; i < vg.getChildCount(); i++) {
        Element child = deflateRelative(vg.getChildAt(i), myTopLeft, doc);
        children.appendChild(child);
      }
    }
    return el;
  }

  private void addExtraValue(Element parent, String name, String value) {
    Element elem = parent.getOwnerDocument().createElement("extra-value");
    elem.setAttribute("key", name);
    elem.setTextContent(value);
    parent.appendChild(elem);
  }

  private void addTextNode(Element parent, String name, String value) {
    Element elem = parent.getOwnerDocument().createElement(name);
    elem.setTextContent(value);
    parent.appendChild(elem);
  }
}
