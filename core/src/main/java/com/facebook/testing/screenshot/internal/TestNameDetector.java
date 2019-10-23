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

package com.facebook.testing.screenshot.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

/** Detect the test name and class that is being run currently. */
public class TestNameDetector {
  private static final String JUNIT_TEST_CASE = "junit.framework.TestCase";
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

  private static @Nullable StackTraceElement getFirstTestElement(StackTraceElement[] stack) {
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
        && (JUNIT_TEST_CASE.equals(clazz.getCanonicalName())
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
