---
id: custom-instrumentation-test-runners
title: Custom InstrumentationTestRunners
layout: docs
permalink: /docs/custom-instrumentation-test-runners/
---

Screenshot tests need a specialized InstrumentationTestRunner. By default this is `ScreenshotTestRunner`.

However very often your organization might already have a custom test runner. In this case you need to override your test runner and call into the Screenshot library's set up and teardown hooks.

```java
public class MyTestRunner extends MyCompanyTestRunner {
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
