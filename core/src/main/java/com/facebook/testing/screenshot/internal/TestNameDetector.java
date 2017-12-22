/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.testing.screenshot.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import junit.framework.TestCase;

/** Detect the test name and class that is being run currently. */
public class TestNameDetector {
  private static final String JUNIT_RUN_WITH = "org.junit.runner.RunWith";
  private static final String JUNIT_TEST = "org.junit.Test";
  private static final String UNKNOWN = "unknown";

  private TestNameDetector() {}

  /**
   * Get the current test class in a standard JUnit3 or JUnit4 test, or "unknown" if we couldn't
   * detect it.
   */
  public static String getTestClass() {
    StackTraceElement element = getFirstTestElement(new Throwable().getStackTrace());
    if (element == null) {
      return UNKNOWN;
    }
    return element.getClassName();
  }

  /**
   * Get the current test name in a standard JUnit3 or JUnit4 test, or "unknown" if we couldn't
   * detect it.
   */
  public static String getTestName() {
    StackTraceElement[] stack = new Throwable().getStackTrace();
    StackTraceElement testElement = getFirstTestElement(stack);
    if (testElement == null) {
      return UNKNOWN;
    }
    String methodName = testElement.getMethodName();
    for (StackTraceElement element : stack) {
      if (testElement.getClassName().equals(element.getClassName())) {
        methodName = element.getMethodName();
      }
    }
    return methodName;
  }

  private static StackTraceElement getFirstTestElement(StackTraceElement[] stack) {
    for (StackTraceElement element : stack) {
      try {
        Class<?> clazz = Class.forName(element.getClassName());
        Method method = clazz.getMethod(element.getMethodName());
        if (isTestClass(clazz) || isTestMethod(method)) {
          return element;
        }
      } catch (NoSuchMethodException ignored) {
        // Not actionable, move onto the next element
      } catch (ClassNotFoundException ignored) {
        // Not actionable, move onto the next element
      }
    }
    return null;
  }

  private static boolean isTestClass(Class<?> clazz) {
    return clazz != null
        && (clazz.equals(TestCase.class)
            || hasAnnotation(clazz.getAnnotations(), JUNIT_RUN_WITH)
            || isTestClass(clazz.getSuperclass()));
  }

  private static boolean isTestMethod(Method method) {
    return hasAnnotation(method.getAnnotations(), JUNIT_TEST);
  }

  private static boolean hasAnnotation(Annotation[] annotations, String annotationCanonicalName) {
    for (Annotation annotation : annotations) {
      if (annotationCanonicalName.equalsIgnoreCase(
          annotation.annotationType().getCanonicalName())) {
        return true;
      }
    }
    return false;
  }
}
