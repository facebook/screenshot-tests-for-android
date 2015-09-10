package com.facebook.testing.screenshot.build

import org.gradle.api.*

class ScreenshotsPluginExtension {
    def adb = "adb"
    def testApkTarget = ":packageDebugAndroidTest"
    def customTestRunner = false
    def recordDir = "screenshots"
}

class ScreenshotsPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.extensions.create("screenshots", ScreenshotsPluginExtension)

    def depTarget = project.screenshots.testApkTarget
    def recordMode = false
    def verifyMode = false

    def codeSource = ScreenshotsPlugin.class.getProtectionDomain().getCodeSource();
    def jarFile = new File(codeSource.getLocation().toURI().getPath());

    println("Found jar file at " + jarFile.getAbsolutePath())

    def implementationVersion = getClass().getPackage().getImplementationVersion()
    project.dependencies.androidTestCompile('com.facebook.testing.screenshot:core:' + implementationVersion)

    project.task('pullScreenshots', dependsOn: depTarget) << {
      def output = project.tasks.getByPath(depTarget).getOutputs().getFiles().getSingleFile().getAbsolutePath()
      println output
      project.exec {
        executable = 'python'

        // I don't know how to only *add* variables, so I'm just propagating all. :(
        environment = [
            'PYTHONPATH': jarFile,
            'ANDROID_HOME': System.getenv('ANDROID_HOME'),
            'ANDROID_SDK': System.getenv('ANDROID_SDK'),
            'PATH': System.getenv('PATH'),
            'ANDROID_SERIAL': System.getenv('ANDROID_SERIAL'),
        ]
        args = ['-m', 'android_screenshot_tests.pull_screenshots', "--apk", output.toString()]

        if (recordMode) {
          args += ["--record", project.screenshots.recordDir]
        } else if (verifyMode) {
          args += ["--verify", project.screenshots.recordDir]
        }
      }
    }

    project.task("clearScreenshots") << {
      project.exec {
        executable = project.screenshots.adb
        args = ["shell", "rm", "-rf", "/sdcard/screenshots"]
      }
    }

    project.task("screenshotTests", dependsOn: [
                   ":clearScreenshots",
                   ":connectedAndroidTest",
                   ":pullScreenshots"])

    if (!project.screenshots.customTestRunner) {
       project.android.defaultConfig {
           testInstrumentationRunner = 'com.facebook.testing.screenshot.ScreenshotTestRunner'
       }
    }

    project.task("recordMode") << {
      recordMode = true
    }

    project.task("verifyMode") << {
      verifyMode = true
    }
  }
}
