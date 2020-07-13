---
layout: home
permalink: index.html
---

### What is this?

Testing rendering for your Android app is hard. How do you prevent visual regressions in paddings and margins and colors from creeping in?

Iterating on UI code is hard. How do you quickly verify that your
layout or view changes work correctly in all configurations?

*screenshot-tests-for-android* can solve these problems by providing a test framework that checks for visual differences across changes.

### How does it work?

*screenshot-tests-for-android* generates deterministic screenshots of views during a test run.

By deterministic, we mean that every single run of your tests generates a pixel-perfect screenshot of the app as it would appear on a user's device. This screenshot can then be used to track changes and write screenshot-base tests.

This is crucial because now you as a developer don't have to worry about threading and timing issues.

We achieve this by mimicing Android's `measure()`, `layout()` and `draw()`
on the test thread, and therefore not having to worry about the impact
of Handler or animation callbacks.

We have utilities to generate reports with all the screenshots, and we also provide a way to plug this into your Continuous Integration, by asking you to "record" screenshots the first time you create them (or whenever you intentionally change them), and letting your continuous integration check against the recorded screenshots.

### Getting Started

First you need to [setup the gradle plugin](#gradle-setup).

Then [you can create a screenshot in a test](#creating-a-screenshot).

We talked about *screenshot-tests-for-android* at Droidcon NYC 2015, and this is a good introduction to the concepts involved, even though the API might be slightly different.

<iframe width="560" height="315" src="https://www.youtube.com/embed/No6iZIbh59Q" frameborder="0" allowfullscreen></iframe>

### Gradle Setup

Setting up *screenshot-tests-for-android* in a Gradle build is very
straightforward if you're already using Gradle and the Android
Gradle plugin. All you need to do is apply the
*screenshot-tests-for-android* plugin in your build.gradle:

```groovy
  buildscript {
    // ...
    dependencies {
      // ...
      classpath 'com.facebook.testing.screenshot:plugin:0.13.0'
    }
  }

  apply plugin: 'com.facebook.testing.screenshot'
```

This plugin sets up a few Gradle tasks:

(_`<App Variant>` is the desired variant to target, for example: `DebugAndroidTest`_)

+ `clean<App Variant>Screenshots` - Clean last generated screenshot report
+ `pull<App Variant>Screenshots` - Pull screenshots from your device
+ `record<App Variant>ScreenshotTest` - Installs and runs screenshot tests, then records their output for later verification
+ `run<App Variant>ScreenshotTest` - Installs and runs screenshot tests, then generates a report
+ `verify<App Variant>ScreenshotTest` - Installs and runs screenshot tests, then verifies their output against previously recorded screenshots

The plugin also sets up compile dependencies for your tests, so you can now just start calling the `Screenshot` API. See the [Creating a screenshot](#creating-a-screenshot) section for more information.

Take a look at our [sample build.gradle](https://github.com/facebook/screenshot-tests-for-android/blob/master/sample/build.gradle).

NOTE: By default this overrides your instrumentation test runner, and depending on your set up this can cause problems. See [Custom Test Runner](#custom-test-runner) for how to avoid this.

**Layout Hierarchy Plugins**

We provide extra plugins for the layout hierarchy viewer included in screenshot test reports to add specific information for custom view types and objects.

Simply include the dependencies:
```groovy
  // Standard Android View Plugins (TextView, etc)
  compile 'com.facebook.testing.screenshot:layout-hierarchy-common:0.13.0'

  // Litho Component Plugins
  compile 'com.facebook.testing.screenshot:layout-hierarchy-litho:0.13.0'
```

And then use the plugins relevant to your test in your test class:
```java
  // Common Plugins
  LayoutHierarchyDumper.addGlobalAttributePlugin(TextViewAttributePlugin.getInstance());

  // Litho plugins
  ComponentsConfiguration.isDebugModeEnabled = true
  LayoutHierarchyDumper.addGlobalHierarchyPlugin(LithoHierarchyPlugin.getInstance())
  LayoutHierarchyDumper.addGlobalAttributePlugin(LithoAttributePlugin.getInstance())
```

You may also create your own plugins by implementing either the `AttributePlugin` or `HierarchyPlugin` interface.

**AndroidManifest permissions**

The screenshots library needs the WRITE_EXTERNAL_STORAGE permission. For an instrumentation test for a library, add this permission to the manifest of the instrumentation APK. For a test for an application, add this permission to the app under test.

### Creating a screenshot

Creating a screenshot from within a test is very easy. You can do this from either JUnit4 style or JUnit3 style instrumentation tests:

```java
public class MyTests {
  @Test
  public void doScreenshot() {
    /*
     * Create and set up your view some how. This might be inflating,
     * or creating from a view class. You might want to set properties
     * on the view.
     */
    View view = mLayoutInflater.inflate(R.layout.my_layout, null, false);

    /*
     * Measure and layout the view. In this example we give an exact
     * width but all the height to be WRAP_CONTENT.
     */
    ViewHelpers.setupView(view)
      .setExactWidthDp(300)
      .layout();

    /*
     * Take the actual screenshot. At the end of this call the screenshot
     * is stored on the device, and the gradle plugin takes care of
     * pulling it and displaying it to you in nice ways.
     */
    Screenshot.snap(view)
      .record();
  }
}
```

We have an example of this in [StandardAndroidViewTest.kt](https://github.com/facebook/screenshot-tests-for-android/blob/master/sample/src/androidTest/java/com/facebook/testing/screenshot/sample/StandardAndroidViewTest.kt). You can generate your screenshots using:

```bash
$ ./gradlew runDebugAndroidTestScreenshotTest
```

For instance, [here's the output generated by our included example test](static/rendering.png).

The report will be saved in a generated temporary directory. The output directory can be changed using the `referenceDir` parameter. The directory will be created if it doesn't exist.

```groovy
screenshots {
  referenceDir = new/target/directory
  // example
  // referenceDir = "$projectDir/build/reports/screenshots"
}
```

### Running Across Multiple Devices

When recording screenshots for later verification, the screenshots are not strictly tied to a specific device by default. If you wish to record screenshots for multiple device configurations and verify each independently, then you should enable `multipleDevices` in your `screenshots` block:

```groovy
screenshots {
  // ...
  multipleDevices true
}
```

Now recorded screenshots will be placed in individual directories based on a unique device identifier. This unique identifier combines the API level, screen density category, availability of Google Play Services, screen resolution and device architecture. For an example, you can take a look at the [sample app's screenshot directory](https://github.com/facebook/screenshot-tests-for-android/tree/master/sample/screenshots/).

By default, screenshots from all connected devices and emulators are pulled. In order to pull screenshots only from some of them, set `ANDROID_SERIAL` environment variable to a comma-separated list of serials (as they appear in the first column of `adb devices` output) of the devices you want to use.

### Custom Test Runner

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

You should make your build.gradle point to your new test runner using `android.testInstrumentationRunner` property.

### Contributions
Use [Github issues](https://github.com/facebook/screenshot-tests-for-android/issues) for requests. We actively welcome pull requests; learn how to [contribute](https://github.com/facebook/screenshot-tests-for-android/blob/master/.github/CONTRIBUTING.md).

### License & Legal

*screenshot-tests-for-android* is [Apache 2 licensed](https://github.com/facebook/screenshot-tests-for-android/blob/master/LICENSE).

[Terms of Service](https://opensource.facebook.com/legal/terms) 

[Privacy Policy](https://opensource.facebook.com/legal/privacy)
