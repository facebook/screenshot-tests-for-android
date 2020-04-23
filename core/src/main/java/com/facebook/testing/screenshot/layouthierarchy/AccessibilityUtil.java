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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
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

  private static final int NODE_INFO_CREATION_RETRY_COUNT = 3;

  private AccessibilityUtil() {}

  /**
   * These roles are defined by Google's TalkBack screen reader, and this list should be kept up to
   * date with their implementation. Details can be seen in their source code here:
   *
   * <p>https://github.com/google/talkback/blob/master/utils/src/main/java/Role.java
   */
  public enum AccessibilityRole {
    NONE(null),
    BUTTON("android.widget.Button"),
    CHECK_BOX("android.widget.CompoundButton"),
    DROP_DOWN_LIST("android.widget.Spinner"),
    EDIT_TEXT("android.widget.EditText"),
    GRID("android.widget.GridView"),
    IMAGE("android.widget.ImageView"),
    IMAGE_BUTTON("android.widget.ImageView"),
    LIST("android.widget.AbsListView"),
    PAGER("androidx.viewpager.widget.ViewPager"),
    RADIO_BUTTON("android.widget.RadioButton"),
    SEEK_CONTROL("android.widget.SeekBar"),
    SWITCH("android.widget.Switch"),
    TAB_BAR("android.widget.TabWidget"),
    TOGGLE_BUTTON("android.widget.ToggleButton"),
    VIEW_GROUP("android.view.ViewGroup"),
    WEB_VIEW("android.webkit.WebView"),
    CHECKED_TEXT_VIEW("android.widget.CheckedTextView"),
    PROGRESS_BAR("android.widget.ProgressBar"),
    ACTION_BAR_TAB("android.app.ActionBar$Tab"),
    DRAWER_LAYOUT("androidx.drawerlayout.widget.DrawerLayout"),
    SLIDING_DRAWER("android.widget.SlidingDrawer"),
    ICON_MENU("com.android.internal.view.menu.IconMenuView"),
    TOAST("android.widget.Toast$TN"),
    DATE_PICKER_DIALOG("android.app.DatePickerDialog"),
    TIME_PICKER_DIALOG("android.app.TimePickerDialog"),
    DATE_PICKER("android.widget.DatePicker"),
    TIME_PICKER("android.widget.TimePicker"),
    NUMBER_PICKER("android.widget.NumberPicker"),
    SCROLL_VIEW("android.widget.ScrollView"),
    HORIZONTAL_SCROLL_VIEW("android.widget.HorizontalScrollView"),
    KEYBOARD_KEY("android.inputmethodservice.Keyboard$Key");

    @Nullable private final String mValue;

    AccessibilityRole(String type) {
      mValue = type;
    }

    @Nullable
    public String getValue() {
      return mValue;
    }

    public static AccessibilityRole fromValue(String value) {
      for (AccessibilityRole role : AccessibilityRole.values()) {
        if (role.getValue() != null && role.getValue().equals(value)) {
          return role;
        }
      }
      return AccessibilityRole.NONE;
    }
  }

  /**
   * Gets the role from a given {@link View}. If no role is defined it will return
   * AccessibilityRole.NONE, which has a value of null.
   *
   * @param view The View to check.
   * @return {@code AccessibilityRole} the defined role.
   */
  public static AccessibilityRole getRole(View view) {
    AccessibilityNodeInfoCompat nodeInfo = createNodeInfoFromView(view);
    if (nodeInfo == null) {
      return AccessibilityRole.NONE;
    }

    AccessibilityRole role = getRole(nodeInfo);
    nodeInfo.recycle();
    return role;
  }

  /**
   * Gets the role from a given {@link AccessibilityNodeInfoCompat}. If no role is defined it will
   * return AccessibilityRole.NONE, which has a value of null.
   *
   * @param node The node to check.
   * @return {@code AccessibilityRole} the defined role.
   */
  public static AccessibilityRole getRole(AccessibilityNodeInfoCompat nodeInfo) {
    AccessibilityRole role = AccessibilityRole.fromValue((String) nodeInfo.getClassName());
    if (role.equals(AccessibilityRole.IMAGE_BUTTON) || role.equals(AccessibilityRole.IMAGE)) {
      return nodeInfo.isClickable() ? AccessibilityRole.IMAGE_BUTTON : AccessibilityRole.IMAGE;
    }

    if (role.equals(AccessibilityRole.NONE)) {
      AccessibilityNodeInfoCompat.CollectionInfoCompat collection = nodeInfo.getCollectionInfo();
      if (collection != null) {
        // RecyclerView will be classified as a list or grid.
        if (collection.getRowCount() > 1 && collection.getColumnCount() > 1) {
          return AccessibilityRole.GRID;
        } else {
          return AccessibilityRole.LIST;
        }
      }
    }

    return role;
  }

  @Nullable
  private static AccessibilityNodeInfoCompat createNodeInfoFromView(@Nullable View view) {
    return createNodeInfoFromView(view, NODE_INFO_CREATION_RETRY_COUNT);
  }

  /**
   * Creates and returns an {@link AccessibilityNodeInfoCompat} from the the provided {@link View}.
   * Note: This does not handle recycling of the {@link AccessibilityNodeInfoCompat}.
   *
   * @param view The {@link View} to create the {@link AccessibilityNodeInfoCompat} from.
   * @param retryCount The number of times to retry creating the AccessibilityNodeInfoCompat.
   * @return {@link AccessibilityNodeInfoCompat}
   */
  @Nullable
  private static AccessibilityNodeInfoCompat createNodeInfoFromView(
      @Nullable View view, int retryCount) {
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
    } catch (RuntimeException e) {
      if (nodeInfo != null) {
        nodeInfo.recycle();
      }
      // For some unknown reason, Android seems to occasionally throw a IndexOutOfBoundsException
      // and also random RuntimeExceptions because the handler seems not to be initialized
      // from onInitializeAccessibilityNodeInfoInternal in ViewGroup.  This seems to be
      // nondeterministic, so lets retry if this happens.
      if (retryCount > 0) {
        return createNodeInfoFromView(view, retryCount - 1);
      }

      return null;
    }

    return nodeInfo;
  }

  /**
   * Returns whether the specified node has text or a content description.
   *
   * @param node The node to check.
   * @return {@code true} if the node has text.
   */
  public static boolean hasText(@Nullable AccessibilityNodeInfoCompat node) {
    return node != null
        && node.getCollectionInfo() == null
        && (!TextUtils.isEmpty(node.getText()) || !TextUtils.isEmpty(node.getContentDescription()));
  }

  /**
   * Returns whether the supplied {@link View} and {@link AccessibilityNodeInfoCompat} would produce
   * spoken feedback if it were accessibility focused. NOTE: not all speaking nodes are focusable.
   *
   * @param view The {@link View} to evaluate
   * @param node The {@link AccessibilityNodeInfoCompat} to evaluate
   * @return {@code true} if it meets the criterion for producing spoken feedback
   */
  public static boolean isSpeakingNode(
      @Nullable AccessibilityNodeInfoCompat node, @Nullable View view) {
    if (node == null || view == null) {
      return false;
    }

    final int important = ViewCompat.getImportantForAccessibility(view);
    if (important == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        || (important == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO && node.getChildCount() <= 0)) {
      return false;
    }

    return node.isCheckable() || hasText(node) || hasNonActionableSpeakingDescendants(node, view);
  }

  /**
   * Determines if the supplied {@link View} and {@link AccessibilityNodeInfoCompat} has any
   * children which are not independently accessibility focusable and also have a spoken
   * description.
   *
   * <p>NOTE: Accessibility services will include these children's descriptions in the closest
   * focusable ancestor.
   *
   * @param view The {@link View} to evaluate
   * @param node The {@link AccessibilityNodeInfoCompat} to evaluate
   * @return {@code true} if it has any non-actionable speaking descendants within its subtree
   */
  public static boolean hasNonActionableSpeakingDescendants(
      @Nullable AccessibilityNodeInfoCompat node, @Nullable View view) {

    if (node == null || view == null || !(view instanceof ViewGroup)) {
      return false;
    }

    final ViewGroup viewGroup = (ViewGroup) view;
    for (int i = 0, count = viewGroup.getChildCount(); i < count; i++) {
      final View childView = viewGroup.getChildAt(i);

      if (childView == null) {
        continue;
      }

      final AccessibilityNodeInfoCompat childNode = createNodeInfoFromView(childView);
      try {
        if (childNode == null) {
          continue;
        }

        if (!childNode.isVisibleToUser()) {
          continue;
        }

        if (isAccessibilityFocusable(childNode, childView)) {
          continue;
        }

        if (isSpeakingNode(childNode, childView)) {
          return true;
        }
      } finally {
        if (childNode != null) {
          childNode.recycle();
        }
      }
    }

    return false;
  }

  /**
   * Determines if the provided {@link View} and {@link AccessibilityNodeInfoCompat} meet the
   * criteria for gaining accessibility focus.
   *
   * <p>Note: this is evaluating general focusability by accessibility services, and does not mean
   * this view will be guaranteed to be focused by specific services such as Talkback. For Talkback
   * focusability, see {@link #isTalkbackFocusable(View)}
   *
   * @param view The {@link View} to evaluate
   * @param node The {@link AccessibilityNodeInfoCompat} to evaluate
   * @return {@code true} if it is possible to gain accessibility focus
   */
  public static boolean isAccessibilityFocusable(
      @Nullable AccessibilityNodeInfoCompat node, @Nullable View view) {
    if (node == null || view == null) {
      return false;
    }

    // Never focus invisible nodes.
    if (!node.isVisibleToUser()) {
      return false;
    }

    // Always focus "actionable" nodes.
    if (isActionableForAccessibility(node)) {
      return true;
    }

    // only focus top-level list items with non-actionable speaking children.
    return isTopLevelScrollItem(node, view) && isSpeakingNode(node, view);
  }

  /**
   * Determines whether the provided {@link View} and {@link AccessibilityNodeInfoCompat} is a
   * top-level item in a scrollable container.
   *
   * @param view The {@link View} to evaluate
   * @param node The {@link AccessibilityNodeInfoCompat} to evaluate
   * @return {@code true} if it is a top-level item in a scrollable container.
   */
  public static boolean isTopLevelScrollItem(
      @Nullable AccessibilityNodeInfoCompat node, @Nullable View view) {
    if (node == null || view == null) {
      return false;
    }

    final View parent = (View) ViewCompat.getParentForAccessibility(view);
    if (parent == null) {
      return false;
    }

    if (node.isScrollable()) {
      return true;
    }

    final List actionList = node.getActionList();
    if (actionList.contains(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
        || actionList.contains(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)) {
      return true;
    }

    // Top-level items in a scrolling pager are actually two levels down since the first
    // level items in pagers are the pages themselves.
    View grandparent = (View) ViewCompat.getParentForAccessibility(parent);
    if (grandparent != null && getRole(grandparent) == AccessibilityRole.PAGER) {
      return true;
    }

    AccessibilityRole parentRole = getRole(parent);
    return parentRole == AccessibilityRole.LIST
        || parentRole == AccessibilityRole.GRID
        || parentRole == AccessibilityRole.SCROLL_VIEW
        || parentRole == AccessibilityRole.HORIZONTAL_SCROLL_VIEW;
  }

  /**
   * Returns whether a node is actionable. That is, the node supports one of {@link
   * AccessibilityNodeInfoCompat#isClickable()}, {@link AccessibilityNodeInfoCompat#isFocusable()},
   * or {@link AccessibilityNodeInfoCompat#isLongClickable()}.
   *
   * @param node The {@link AccessibilityNodeInfoCompat} to evaluate
   * @return {@code true} if node is actionable.
   */
  public static boolean isActionableForAccessibility(@Nullable AccessibilityNodeInfoCompat node) {
    if (node == null) {
      return false;
    }

    if (node.isClickable() || node.isLongClickable() || node.isFocusable()) {
      return true;
    }

    final List actionList = node.getActionList();
    return actionList.contains(AccessibilityNodeInfoCompat.ACTION_CLICK)
        || actionList.contains(AccessibilityNodeInfoCompat.ACTION_LONG_CLICK)
        || actionList.contains(AccessibilityNodeInfoCompat.ACTION_FOCUS);
  }

  /**
   * Determines if any of the provided {@link View}'s and {@link AccessibilityNodeInfoCompat}'s
   * ancestors can receive accessibility focus
   *
   * @param view The {@link View} to evaluate
   * @param node The {@link AccessibilityNodeInfoCompat} to evaluate
   * @return {@code true} if an ancestor of may receive accessibility focus
   */
  public static boolean hasFocusableAncestor(
      @Nullable AccessibilityNodeInfoCompat node, @Nullable View view) {
    if (node == null || view == null) {
      return false;
    }

    final ViewParent parentView = ViewCompat.getParentForAccessibility(view);
    if (!(parentView instanceof View)) {
      return false;
    }

    final AccessibilityNodeInfoCompat parentNode = createNodeInfoFromView((View) parentView);
    try {
      if (parentNode == null) {
        return false;
      }

      if (areBoundsIdenticalToWindow(parentNode, (View) parentView)
          && parentNode.getChildCount() > 0) {
        return false;
      }

      if (isAccessibilityFocusable(parentNode, (View) parentView)) {
        return true;
      }

      if (hasFocusableAncestor(parentNode, (View) parentView)) {
        return true;
      }
    } finally {
      if (parentNode != null) {
        parentNode.recycle();
      }
    }
    return false;
  }

  /**
   * Returns whether a AccessibilityNodeInfoCompat has the same size and position as its containing
   * Window.
   *
   * @param node The {@link AccessibilityNodeInfoCompat} to evaluate
   * @return {@code true} if node has equal bounds to its containing Window
   */
  public static boolean areBoundsIdenticalToWindow(AccessibilityNodeInfoCompat node, View view) {
    Window window = null;
    Context context = view.getContext();
    while (context instanceof ContextWrapper) {
      if (context instanceof Activity) {
        window = ((Activity) context).getWindow();
      }
      context = ((ContextWrapper) context).getBaseContext();
    }

    if (window == null) {
      return false;
    }

    WindowManager.LayoutParams windowParams = window.getAttributes();
    Rect windowBounds =
        new Rect(
            windowParams.x,
            windowParams.y,
            windowParams.x + windowParams.width,
            windowParams.y + windowParams.height);

    Rect nodeBounds = new Rect();
    node.getBoundsInScreen(nodeBounds);

    return windowBounds.equals(nodeBounds);
  }

  /**
   * Returns whether a View has any children that are visible.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if node has any visible children
   */
  public static boolean hasVisibleChildren(View view) {
    if (!(view instanceof ViewGroup)) {
      return false;
    }

    ViewGroup viewGroup = (ViewGroup) view;
    int childCount = viewGroup.getChildCount();
    for (int i = 0; i < childCount; ++i) {
      AccessibilityNodeInfoCompat childNodeInfo = createNodeInfoFromView(viewGroup.getChildAt(i));
      if (childNodeInfo != null) {
        try {
          if (childNodeInfo.isVisibleToUser()) {
            return true;
          }
        } finally {
          childNodeInfo.recycle();
        }
      }
    }

    return false;
  }

  /**
   * Returns whether a given {@link View} will be focusable by Google's TalkBack screen reader.
   *
   * @param view The {@link View} to evaluate.
   * @return {@code boolean} if the view will be ignored by TalkBack.
   */
  public static boolean isTalkbackFocusable(View view) {
    if (view == null) {
      return false;
    }

    final int important = ViewCompat.getImportantForAccessibility(view);
    if (important == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO
        || important == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS) {
      return false;
    }

    // Go all the way up the tree to make sure no parent has hidden its descendants
    ViewParent parent = view.getParent();
    while (parent instanceof View) {
      if (ViewCompat.getImportantForAccessibility((View) parent)
          == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS) {
        return false;
      }
      parent = parent.getParent();
    }

    // Trying to evaluate the focusability of certain element types (mainly list views) can cause
    // problems when trying to determine the offset of the elements Rect relative to its parent in
    // ViewGroup.offsetRectBetweenParentAndChild. If this happens, simply return false, as this view
    // will not be focusable.
    AccessibilityNodeInfoCompat node;
    try {
      node = createNodeInfoFromView(view);
    } catch (IllegalArgumentException e) {
      return false;
    }

    if (node == null) {
      return false;
    }

    // Non-leaf nodes identical in size to their Window should not be focusable.
    if (areBoundsIdenticalToWindow(node, view) && node.getChildCount() > 0) {
      return false;
    }

    try {
      if (!node.isVisibleToUser()) {
        return false;
      }

      if (isAccessibilityFocusable(node, view)) {
        if (!hasVisibleChildren(view)) {
          // Leaves that are accessibility focusable are never ignored, even if they don't have a
          // speakable description
          return true;
        } else if (isSpeakingNode(node, view)) {
          // Node is focusable and has something to speak
          return true;
        }

        // Node is focusable and has nothing to speak
        return false;
      }

      // if view is not accessibility focusable, it needs to have text and no focusable ancestors.
      if (!hasText(node)) {
        return false;
      }

      if (!hasFocusableAncestor(node, view)) {
        return true;
      }

      return false;
    } finally {
      node.recycle();
    }
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
