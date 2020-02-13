# Screenshot Tests for Android

<img src="/.github/logo.png" width="150" align="right"/>

screenshot-tests-for-android is a library that can generate fast
deterministic screenshots while running instrumentation tests on
Android.

We mimic Android's measure(), layout() and draw() to generate screenshots
on the test thread. By not having to do the rendering on a separate
thread we have control over animations and handler callbacks which
makes the screenshots extremely deterministic and reliable for catching
regressions in continuous integration.

We also provide utilities for using screenshot tests during the development
process. With these scripts you can iterate on a view or layout and quickly
see how the view renders in a real Android environment, without having to
build the whole app. You can also render the view in multiple configurations
at one go.

## Documentation

Take a look at the documentation at http://facebook.github.io/screenshot-tests-for-android/#getting-started

## Requirements

screenshot-tests-for-android is known to work with MacOS or Linux.

The host tooling probably doesn't work on Windows, but can be made to
work with a little effort. We'll happily accept pull requests!

You need python-2.7 for the gradle plugin to work, and we also
recommending installing the python-pillow library which is required
for recording and verifying screenshots.

## Building screenshot-tests-for-android

You don't have to build screenshot-tests-for-android from scratch if
you don't plan to contribute. All artifacts are available from Maven
Central.

If you plan to contribute, this is the code is broken up into a few modules:

* The `core` module is packaged as part of your instrumentation tests
  and generates screenshots on the device.

* The `plugin` module adds Gradle tasks to make it easier to work
  with screenshot tests.

* The `layout-hierarchy-common` module adds extra common `View` information to your reports' layout hierarchy viewer

* The `layout-hierarchy-litho` module adds extra Litho component information to your reports' layout hierarchy viewer


In addition you'll find python code inside `plugin/src/py`. This code
is packaged into the gradle plugin.

We have tests for the python code and the core library. Run these
commands to run all the tests:

```bash
  $ gradle :plugin:pyTests
  $ gradle :core:connectedAndroidTest
```

Both need a running emulator. 

Python tests rely on the `mock` and `Pillow` libraries. Both can be installed via `pip install mock`
and `pip install Pillow`.

You can install all the artifacts to your local maven repository using

```bash
  $ gradle installArchives
```

## Running With a Remote Service

For usage with a remote testing service (e.g. Google Cloud Test Lab) where ADB is not available directly the plugin supports a "disconnected" 
workflow.  Collect all screenshots into a single directory and run the plugin using the following options

### Example
The location of the screenshot artifacts can be configured in the project's build.gradle:
```groovy
  screenshots {
      // Points to the directory containing all the files pulled from a device
      referenceDir = path/to/screenshots
  }
```

Then, screenshots may be verified by executing the following:
```bash
  $ gradle :<project>:verify<flavor>ScreenshotTest
```

To record, simply change `verify` to `record`.

## Contributing

Please see the [contributing](.github/CONTRIBUTING.md) file.

## Authors

screenshot-tests-for-android was originally written by Arnold Noronha (arnold@tdrhq.com)
You can reach him at @tdrhq on GitHub.

It is currently maintained by Hilal Alsibai (@xiphirx)

## License

screenshot-tests-for-android is Apache-2-licensed.
