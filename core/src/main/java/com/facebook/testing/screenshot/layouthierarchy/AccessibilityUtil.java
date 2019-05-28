/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.testing.screenshot.layouthierarchy;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * This class provides utility methods for determining certain accessibility properties of {@link
 * View}s and {@link AccessibilityNodeInfoCompat}s. It is porting some of the checks from {@link
 * com.googlecode.eyesfree.utils.AccessibilityNodeInfoUtils}, but has stripped many features which
 * are unnecessary here.
 */
public class AccessibilityUtil {

  private AccessibilityUtil() {}

  /**
   * Creates and returns an {@link AccessibilityNodeInfoCompat} from the the provided {@link View}.
   * Note: This does not handle recycling of the {@link AccessibilityNodeInfoCompat}.
   *
   * @param view The {@link View} to create the {@link AccessibilityNodeInfoCompat} from.
   * @return {@link AccessibilityNodeInfoCompat}
   */
  @Nullable
  private static AccessibilityNodeInfoCompat createNodeInfoFromView(@Nullable View view) {
    if (view == null) {
      return null;
    }

    final AccessibilityNodeInfoCompat nodeInfo = AccessibilityNodeInfoCompat.obtain();

    // For some unknown reason, Android seems to occasionally throw a NPE from
    // onInitializeAccessibilityNodeInfo.
    try {
      ViewCompat.onInitializeAccessibilityNodeInfo(view, nodeInfo);
    } catch (NullPointerException e) {
      if (nodeInfo != null) {
        nodeInfo.recycle();
      }
      return null;
    }

    return nodeInfo;
  }

  public static AXTreeNode generateAccessibilityTree(View view, @Nullable AXTreeNode parent) {
    AXTreeNode axTree = new AXTreeNode(view, parent);

    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        AXTreeNode descendantTree = generateAccessibilityTree(viewGroup.getChildAt(i), axTree);
        axTree.addChild(descendantTree);
      }
    }

    return axTree;
  }

  public static class AXTreeNode {
    private final View mView;
    private final @Nullable AccessibilityNodeInfoCompat mNodeInfo;
    private final List<AXTreeNode> mChildren = new ArrayList<>();
    private final @Nullable AXTreeNode mParent;

    public AXTreeNode(View view, @Nullable AXTreeNode parent) {
      mView = view;
      mNodeInfo = createNodeInfoFromView(view);
      mParent = parent;
    }

    public View getView() {
      return mView;
    }

    public @Nullable AccessibilityNodeInfoCompat getNodeInfo() {
      return mNodeInfo;
    }

    public List<AXTreeNode> getChildren() {
      return mChildren;
    }

    public int getChildCount() {
      return mChildren.size();
    }

    public void addChild(AXTreeNode child) {
      mChildren.add(child);
    }

    public @Nullable AXTreeNode getParent() {
      return mParent;
    }

    public List<AXTreeNode> getAllNodes() {
      List<AXTreeNode> nodes = new ArrayList<>();
      addAllNodes(nodes);
      return nodes;
    }

    public void addAllNodes(List<AXTreeNode> nodes) {
      nodes.add(this);
      for (AXTreeNode child : mChildren) {
        child.addAllNodes(nodes);
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      toStringInner(sb, "");
      return sb.toString();
    }

    private void toStringInner(StringBuilder sb, String indent) {
      sb.append(getView().getClass().getSimpleName());
      String nextIndent = indent + "  ";
      for (AXTreeNode child : mChildren) {
        sb.append('\n');
        sb.append(indent);
        sb.append("-> ");
        child.toStringInner(sb, nextIndent);
      }
    }
  }
}
