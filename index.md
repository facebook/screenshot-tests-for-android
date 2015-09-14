---
title: screenshot-tests-for-android | Fast, deterministic screenshot tests for android
layout: home
permalink: index.html
---

### About
screenshot-tests-for-android is a library that can generate fast deterministic screenshots while running instrumentation tests in android.

We mimic Android's measure(), layout() and draw() to generate screenshots on the test thread. By not having to do the rendering on a separate thread we have control over animations and handler callbacks which makes the screenshots extremely deterministic and reliable for catching regressions in continuous integration.

We also provide utilities for using screenshot tests during the development process. With these scripts you can iterate on a view or layout and quickly see how the view renders in real android code, without having to build the whole app. You can also render the view in multiple configurations at one go.

### Contributions
Use [Github issues](https://github.com/facebook/screenshot-tests-for-android/issues) for requests. We actively welcome pull requests; learn how to [contribute](https://github.com/facebook/screenshot-tests-for-android/blob/master/CONTRIBUTING.md).

###License

screenshot-tests-for-android is [BSD-licensed](https://github.com/facebook/screenshot-tests-for-android/blob/master/LICENSE). We also provide an additional [patent grant](https://github.com/facebook/screenshot-tests-for-android/blob/master/PATENTS).
