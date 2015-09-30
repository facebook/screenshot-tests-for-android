---
id: gradle-setup
title: Gradle Setup
layout: docs
permalink: /docs/gradle-setup/
---

Setting up screenshot-tests-for-android in a gradle build is very
straightforward if you're already using Gradle and the Android's
Gradle plugin. All you need to do is apply the
screenshot-tests-for-android plugin in your build.gradle:

```groovy
  buildscript {
    // ...
    dependencies {
      // ...
      classpath 'com.facebook.testing.screenshot:plugin:0.2.1'
    }
  }

  apply plugin: 'com.facebook.testing.screenshot'
```

This plugin sets up a few convenience commands:

`gradle screenshotTests` will run all the instrumentation tests, and then generate a report of all of the screenshots.

`gradle recordMode screenshotTests` will run all the screenshot tests and record all the screenshots in your `screenshots/`. You can commit this directory into your repository.

`gradle verifyMode screenshotTests` runs all the screenshot tests and compares it against the previously recorded screenshots. If any of them fails, this command will fail. We expect you to run this command in continuous integration.

The plugin also sets up compile dependencies for your tests, so you can now just start calling the `Screenshot` API. See [Creating a screenshot](docs/creating-a-screenshot/).

Take a look at our [example build.gradle](https://github.com/facebook/screenshot-tests-for-android/blob/master/examples/app-example/build.gradle).

NOTE: By default this overrides your instrumentation test runner, and depending on your set up this can cause problems. See [Custom InstrumentationTestRunners](docs/custom-instrumentation-test-runners/) for how to avoid this.


== AndroidManifest permissions ==

The screenshots library needs the WRITE_EXTERNAL_STORAGE permission. For an instrumentation test for a library, add this permission to the manifest of the instrumentation apk. For a test for an application, add this permission to the app under test.