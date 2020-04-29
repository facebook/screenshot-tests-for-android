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

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.RangeInfoCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Dumps information about the accessibility hierarchy into a JSON object */
public final class AccessibilityHierarchyDumper {

  AccessibilityHierarchyDumper() {}

  public static JSONObject dumpHierarchy(AccessibilityUtil.AXTreeNode axTree) throws JSONException {
    JSONObject root = new JSONObject();
    if (axTree == null) {
      return root;
    }

    View view = axTree.getView();
    AccessibilityNodeInfoCompat nodeInfo = axTree.getNodeInfo();

    root.put("class", view.getClass().getName());

    if (nodeInfo != null) {
      if (nodeInfo.getActionList().size() == 0) {
        root.put("actionList", JSONObject.NULL);
      } else {
        JSONArray actionList = new JSONArray();
        for (AccessibilityActionCompat action : nodeInfo.getActionList()) {
          actionList.put(action.getId());
        }
        root.put("actionList", actionList);
      }

      Rect tempRect = new Rect();
      nodeInfo.getBoundsInParent(tempRect);
      JSONObject parentBoundsObj = new JSONObject();
      parentBoundsObj.put("left", tempRect.left);
      parentBoundsObj.put("right", tempRect.right);
      parentBoundsObj.put("top", tempRect.top);
      parentBoundsObj.put("bottom", tempRect.bottom);
      root.put("boundsInParent", parentBoundsObj);

      nodeInfo.getBoundsInScreen(tempRect);
      JSONObject screenBoundsObj = new JSONObject();
      screenBoundsObj.put("left", tempRect.left);
      screenBoundsObj.put("right", tempRect.right);
      screenBoundsObj.put("top", tempRect.top);
      screenBoundsObj.put("bottom", tempRect.bottom);
      root.put("boundsInScreen", screenBoundsObj);

      root.put("canOpenPopup", nodeInfo.canOpenPopup());
      root.put("childCount", nodeInfo.getChildCount());
      root.put("className", nodeInfo.getClassName());

      if (nodeInfo.getCollectionInfo() == null) {
        root.put("collectionInfo", JSONObject.NULL);
      } else {
        JSONObject collectionInfoObj = new JSONObject();
        CollectionInfoCompat collectionInfo = nodeInfo.getCollectionInfo();
        collectionInfoObj.put("columnCount", collectionInfo.getColumnCount());
        collectionInfoObj.put("rowCount", collectionInfo.getRowCount());
        collectionInfoObj.put("selectionMode", collectionInfo.getSelectionMode());
        collectionInfoObj.put("isHierarchical", collectionInfo.isHierarchical());
        root.put("collectionInfo", collectionInfoObj);
      }

      if (nodeInfo.getCollectionItemInfo() == null) {
        root.put("collectionItemInfo", JSONObject.NULL);
      } else {
        JSONObject collectionItemInfoObj = new JSONObject();
        CollectionItemInfoCompat collectionItemInfo = nodeInfo.getCollectionItemInfo();
        collectionItemInfoObj.put("columnIndex", collectionItemInfo.getColumnIndex());
        collectionItemInfoObj.put("columnSpan", collectionItemInfo.getColumnSpan());
        collectionItemInfoObj.put("rowIndex", collectionItemInfo.getRowIndex());
        collectionItemInfoObj.put("rowSpan", collectionItemInfo.getRowSpan());
        collectionItemInfoObj.put("isHeading", collectionItemInfo.isHeading());
        collectionItemInfoObj.put("isSelected", collectionItemInfo.isSelected());
        root.put("collectionItemInfo", collectionItemInfoObj);
      }

      root.put("contentDescription", jsonNullOr(nodeInfo.getContentDescription()));
      root.put("error", jsonNullOr(nodeInfo.getError()));

      if (nodeInfo.getExtras() == null) {
        root.put("extras", JSONObject.NULL);
      } else {
        Bundle extras = nodeInfo.getExtras();
        root.put("extras", extras.toString());
      }

      root.put("inputType", nodeInfo.getInputType());
      root.put("isCheckable", nodeInfo.isCheckable());
      root.put("isChecked", nodeInfo.isChecked());
      root.put("isClickable", nodeInfo.isClickable());
      root.put("isContentInvalid", nodeInfo.isContentInvalid());
      root.put("isDismissable", nodeInfo.isDismissable());
      root.put("isEditable", nodeInfo.isEditable());
      root.put("isEnabled", nodeInfo.isEnabled());
      root.put("isFocusable", nodeInfo.isFocusable());
      root.put("isImportantForAccessibility", nodeInfo.isImportantForAccessibility());
      root.put("isLongClickable", nodeInfo.isLongClickable());
      root.put("isMultiLine", nodeInfo.isMultiLine());
      root.put("isPassword", nodeInfo.isPassword());
      root.put("isScrollable", nodeInfo.isScrollable());
      root.put("isSelected", nodeInfo.isSelected());
      root.put("isVisibleToUser", nodeInfo.isVisibleToUser());
      root.put("liveRegion", nodeInfo.getLiveRegion());
      root.put("maxTextLength", nodeInfo.getMaxTextLength());
      root.put("movementGranularities", nodeInfo.getMovementGranularities());

      if (nodeInfo.getRangeInfo() == null) {
        root.put("rangeInfo", JSONObject.NULL);
      } else {
        JSONObject rangeInfoObj = new JSONObject();
        RangeInfoCompat rangeInfo = nodeInfo.getRangeInfo();
        rangeInfoObj.put("current", rangeInfo.getCurrent());
        rangeInfoObj.put("max", rangeInfo.getMax());
        rangeInfoObj.put("min", rangeInfo.getMin());
        rangeInfoObj.put("type", rangeInfo.getType());
        root.put("rangeInfo", rangeInfoObj);
      }

      root.put("text", jsonNullOr(nodeInfo.getText()));

      nodeInfo.recycle();
    }

    if (axTree.getChildCount() > 0) {
      JSONArray children = new JSONArray();
      for (AccessibilityUtil.AXTreeNode child : axTree.getChildren()) {
        JSONObject childSerialization = dumpHierarchy(child);
        children.put(childSerialization);
      }
      root.put("children", children);
    } else {
      root.put("children", JSONObject.NULL);
    }

    return root;
  }

  public static JSONObject dumpHierarchy(View view) throws JSONException {
    return dumpHierarchy(AccessibilityUtil.generateAccessibilityTree(view, null));
  }

  private static Object jsonNullOr(Object obj) {
    return obj == null ? JSONObject.NULL : obj;
  }
}
