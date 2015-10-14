package com.facebook.testing.screenshot.build

import org.gradle.api.*

class ScreenshotsPluginExtension {
    def adb = "adb"
    def testApkTarget = "packageDebugAndroidTest"
    def customTestRunner = false
    def recordDir = "screenshots"
    def addCompileDeps = true
}

class ScreenshotsPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.extensions.create("screenshots", ScreenshotsPluginExtension)

    def recordMode = false
    def verifyMode = false

    def codeSource = ScreenshotsPlugin.class.getProtectionDomain().getCodeSource();
    def jarFile = new File(codeSource.getLocation().toURI().getPath());

    if (project.screenshots.addCompileDeps) {
      addRuntimeDep(project)
    }

    project.task('pullScreenshots') << {
      project.exec {
        def output = getTestApkOutput(project)

        executable = 'python'

        // I don't know how to only *add* variables, so I'm just propagating all. :(
        environment = [
            'PYTHONPATH': jarFile,
            'ANDROID_HOME': System.getenv('ANDROID_HOME'),
            'ANDROID_SDK': System.getenv('ANDROID_SDK'),
            'PATH': System.getenv('PATH'),
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
        ignoreExitValue = true
      }
    }

    project.afterEvaluate {
      project.task("screenshotTests")
      project.screenshotTests.dependsOn project.clearScreenshots
      project.screenshotTests.dependsOn project.connectedAndroidTest
      project.screenshotTests.dependsOn project.pullScreenshots

      project.pullScreenshots.dependsOn project.screenshots.testApkTarget
    }

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

  String getTestApkOutput(Project project) {
    return project.tasks.getByPath(project.screenshots.testApkTarget).getOutputs().getFiles().getSingleFile().getAbsolutePath()
  }

  void addRuntimeDep(Project project) {
    def implementationVersion = getClass().getPackage().getImplementationVersion()

    if (!implementationVersion) {
      println("WARNING: you shouldn't see this in normal operation, file a bug report if this is not a framework test")
      implementationVersion = '0.2.2'
    }

    project.dependencies.androidTestCompile('com.facebook.testing.screenshot:core:' + implementationVersion)
  }
}
