# Contributing to screenshot-tests-for-android
We want to make contributing to this project as easy and transparent as
possible.

## Our Development Process

Our internal repository, which is copied to GitHub, is our source of truth, 
and development happens both directly in GitHub and internally. 
Internally, we might build tools around this framework that we might move 
into the GitHub repository in the future, but we won't fork for internal changes.

This repository has two components:

* in `core/` you'll find code that actually runs on the device along
  with the test

* in `plugin/` you'll find code that runs on the "host" machine.

The 'plugin' code is broken into Groovy code that runs as part of your
Gradle build, and in `src/py` you'll find python code that actually
does the heavy work of pulling images and generating HTML files.

We encourage tests for any pull request, tests can be run with

  ./gradlew connectedCheck -i

## Pull Requests
We actively welcome your pull requests.

1. Fork the repo and create your branch from `main`.
2. If you've added code that should be tested, add tests
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes.
5. Make sure your code lints.
6. If you haven't already, complete the Contributor License Agreement ("CLA").

## Contributor License Agreement ("CLA")
In order to accept your pull request, we need you to submit a CLA. You only need
to do this once to work on any of Facebook's open source projects.

Complete your CLA here: <https://code.facebook.com/cla>

## Issues
We use GitHub issues to track public bugs. Please ensure your description is
clear and has sufficient instructions to be able to reproduce the issue.

Facebook has a [bounty program](https://www.facebook.com/whitehat/) for the safe
disclosure of security bugs. In those cases, please go through the process
outlined on that page and do not file a public issue.

## Coding Style
* We use Google's Java formatter (https://github.com/google/google-java-format) with default settings. Please use it to format your changes.

## License
By contributing screenshot-tests-for-android, you agree that your contributions will be licensed
under its Apache 2 license.
