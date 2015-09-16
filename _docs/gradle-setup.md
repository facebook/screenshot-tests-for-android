---
id: gradle-setup
title: Gradle Setup
layout: docs
permalink: /docs/gradle-setup/
---

Setting up screenshot-tests-for-android in a gradle build is very straightforward. Assuming you're using the official android plugin, you just need to apply our plugin:

```groovy
  buildscript {
    // ...
    dependencies {
      // ...
      classpath 'com.facebook.testing.screenshot:plugin:0.2.1'
    }
  }

  apply plugin: 'screenshot-tests-plugin'
```

By default this overrides your instrumentation test runner, and depending on your set up this can cause problems. See [Custom InstrumentationTestRunners](docs/custom-instrumentation-test-runners/) for how to avoid this.

This plugin sets up a few convenience commands:

`gradle screenshotTests` will run all the instrumentation tests, and then generate a report of all of the screenshots.

`gradle recordMode screenshotTests` will run all the screenshot tests and record all the screenshots in your `screenshots/`. You can commit this directory into your repository.

`gradle verifyMode screenshotTests` runs all the screenshot tests and compares it against the previously recorded screenshots. If any of them fails, this command will fail. We expect you to run this command in continuous integration.

The plugin also sets up compile dependencies for your tests, so you can now just start calling the `Screenshot` API.See [Creating a screenshot](docs/creating-a-screenshot/).
