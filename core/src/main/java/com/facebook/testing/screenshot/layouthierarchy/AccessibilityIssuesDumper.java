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

package com.facebook.testing.screenshot.layouthierarchy;

import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import javax.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class AccessibilityIssuesDumper {

  AccessibilityIssuesDumper() {}

  public static JSONArray dumpIssues(AccessibilityUtil.AXTreeNode axTree) throws JSONException {
    JSONArray root = new JSONArray();

    JSONObject focusableElementsWithoutFeedback =
        findTalkbackFocusableElementsWithoutSpokenFeedback(axTree);
    if (focusableElementsWithoutFeedback != null) {
      root.put(focusableElementsWithoutFeedback);
    }

    return root;
  }

  private static @Nullable JSONObject findTalkbackFocusableElementsWithoutSpokenFeedback(
      AccessibilityUtil.AXTreeNode axTree) throws JSONException {
    JSONObject evaluation = new JSONObject();
    evaluation.put("id", "talkback_focusable_element_without_spoken_feedback");
    evaluation.put("name", "Focusable Element Without Spoken Feedback");
    evaluation.put(
        "description",
        "The element is focusable by screen readers such as Talkback, but has no text to "
            + "announce.");

    JSONArray elements = new JSONArray();
    for (AccessibilityUtil.AXTreeNode axTreeNode : axTree.getAllNodes()) {
      View view = axTreeNode.getView();
      AccessibilityNodeInfoCompat nodeInfo = axTreeNode.getNodeInfo();
      if (AccessibilityUtil.isTalkbackFocusable(view)
          && !AccessibilityUtil.isSpeakingNode(nodeInfo, view)) {
        JSONObject element = new JSONObject();
        element.put("name", view.getClass().getSimpleName());
        element.put("class", view.getClass().getName());
        JSONObject elementPos = new JSONObject();
        elementPos.put("left", view.getLeft());
        elementPos.put("top", view.getTop());
        elementPos.put("width", view.getWidth());
        elementPos.put("height", view.getHeight());
        element.put("position", elementPos);
        JSONArray suggestions = new JSONArray();
        suggestions.put("Add a contentDescription to the element.");
        if (view instanceof ViewGroup) {
          suggestions.put("Add a contentDescription or visible text to a child element.");
        }
        element.put("suggestions", suggestions);
        elements.put(element);
      }
    }

    if (elements.length() > 0) {
      evaluation.put("elements", elements);
      return evaluation;
    }

    return null;
  }
}
