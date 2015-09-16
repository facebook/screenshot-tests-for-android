---
id: custom-instrumentation-test-runners
title: Custom InstrumentationTestRunners
layout: docs
permalink: /docs/custom-instrumentation-test-runners/
---

By default, screenshot tests use a specialized
InstrumentationTestRunner, which is [ScreenshotTestRunner](https://github.com/facebook/screenshot-tests-for-android/blob/master/core/src/main/java/com/facebook/testing/screenshot/ScreenshotTestRunner.java).

However, very often your organization might already have a custom test
runner. Or more typically, you might be using
android.support.test.runner.AndroidJUnitRunner, which provides JUnit4
support. In this case you need to override your test runner and call
into the Screenshot library's set up and teardown hooks.

```java
public class MyTestRunner extends AndroidJUnitRunner {
  @Override
  public void onCreate(Bundle args) {
    ScreenshotRunner.onCreate(this, args);
    super.onCreate(args);
  }

  @Override
  public void finish(int resultCode, Bundle results) {
    ScreenshotRunner.onDestroy();
    super.finish(resultCode, results);
  }
}
```

You should make your build.gradle point to your new test runner using
`android.testInstrumentationRunner` property.
