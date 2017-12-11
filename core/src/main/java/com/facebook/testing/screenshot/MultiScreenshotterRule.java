// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.testing.screenshot;

import android.app.Activity;
import com.facebook.testing.screenshot.internal.TestNameDetector;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 *
 * <pre>
 * Captures multiple screenshots in the same instrumentation test case.
 *
 * Typically, this class is used as following:
 *
 * Rule MultiScreenshotter mScreenshotter = new MultiScreenshotter();
 * mScreenshotter.snap(activity, "beforeStart")
 * mScreenshotter.snap(activity, "started")
 * </pre>
 */
public class MultiScreenshotterRule implements TestRule {

  private final Map<String, AtomicInteger> mTestCounterMap =
      Collections.synchronizedMap(new HashMap<String, AtomicInteger>());

  @Override
  public Statement apply(final Statement base, final Description description) {
    return base;
  }

  /**
   * Takes a screen shot, counts the number of screenshots done in the test method and names the
   * screenshot in following format:
   *
   * <p>{test class name}_{test name}_{screen shot count for the test}_{given name} Ex:
   * LithoAnimationsTest_testIncrementalMountDuringAnimation_1.beforeStart
   *
   * @param activity the activity whose screenshot is taken
   * @param name given name for the screenshot
   */
  public void snap(Activity activity, String name) {
    int count;
    String testName = getTestName();
    if (!mTestCounterMap.containsKey(testName)) {
      count = 1;
      mTestCounterMap.put(testName, new AtomicInteger(count));
    } else {
      count = mTestCounterMap.get(testName).addAndGet(1);
    }

    String ssName = testName + "_" + count + "." + name;

    Screenshot.snapActivity(activity).setName(ssName).record();

    ++count;
  }

  private static String getTestName() {
    return TestNameDetector.getTestClass() + "_" + TestNameDetector.getTestName();
  }
}
