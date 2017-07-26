/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.testing.screenshot.internal;

import android.util.Log;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

/**
 * Detect the test name and class that is being run currently.
 */
public class TestNameDetector {
  private static final String UNKNOWN = "unknown";

  private TestNameDetector() {
  }

  /**
   * Get the current test class in a standard JUnit3 or JUnit4 test,
   * or "unknown" if we couldn't detect it.
   */
  public static String getTestClass() {
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    for (StackTraceElement elem : stack) {
      try {
        if (isTestElement(elem)) {
          return elem.getClassName();
        }
      } catch (ClassNotFoundException c) {
        Log.e("ScreenshotImpl", "Class not found in stack", c);
        return UNKNOWN;
      }
    }
    return "unknown";
  }

  /**
   * Get the current test name in a standard JUnit3 or JUnit4 test, or
   * "unknown" if we couldn't detect it.
   */
  public static String getTestName() {
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    String testClass = getTestClass();

    // Find the first call from this class:
    String finalName = UNKNOWN;
    for (StackTraceElement elem : stack) {
      if (testClass.equals(elem.getClassName())) {
        finalName = elem.getMethodName();
      }
    }
    return finalName;
  }

  private static boolean isTestCase(Class<?> clazz) {
    if (clazz.equals(TestCase.class) || clazz.getAnnotation(RunWith.class) != null) {
      return true;
    }

    if (clazz.equals(Object.class)) {
      return false;
    }

    return isTestCase(clazz.getSuperclass());
  }

  private static boolean isTestElement(StackTraceElement elem) throws ClassNotFoundException {
    try {
      Class<?> clazz = Class.forName(elem.getClassName());
      Method method = clazz.getMethod(elem.getMethodName());
      return isTestCase(clazz) || method.getAnnotation(Test.class) != null;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }
}
