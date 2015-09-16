---
title: screenshot-tests-for-android | Fast, deterministic screenshot tests for android
layout: home
permalink: index.html
---

### Introduction

Testing *rendering* for your Android app is hard. How do you prevent regressions in paddings and margins and colors from creeping in?

Iterating on UI code is hard. How do you quickly verify that your
layout or view changes work correctly in all the configurations?

We introduce screenshot-tests-for-android, and we hope to solve these
problems for you.

### How does this work?

screenshot-tests-for-android is a library that generates
*deterministic* screenshots of views during a test run.

By determinism, we mean that every run of your tests generate a
pixel-identical screenshot. This is crucial for being able to use
these screenshots to track changes. It's also crucial for making it
easy to write a screenshot test, because now you as a developer don't
have to worry about threading and timing issues.

We achieve this by mimicing Android's measure(), layout() and draw()
on the test thread, and therefore not having to worry about the impact
of Handler or animation callbacks.

We have utitlities to generate reports with all the screenshots. We
also provide a way to plug this into your Continuous Integration, by
asking you to "record" screenshots the first time you create them (or
if you intentionally change them), and letting your continuous
integration check against the recorded screenshots.

### Contributions
Use [Github issues](https://github.com/facebook/screenshot-tests-for-android/issues) for requests. We actively welcome pull requests; learn how to [contribute](https://github.com/facebook/screenshot-tests-for-android/blob/master/CONTRIBUTING.md).

###License

screenshot-tests-for-android is [BSD-licensed](https://github.com/facebook/screenshot-tests-for-android/blob/master/LICENSE). We also provide an additional [patent grant](https://github.com/facebook/screenshot-tests-for-android/blob/master/PATENTS).
