package com.facebook.testing.screenshot.build

import org.gradle.api.*

class ScreenshotsPluginExtension {
    def bin = "./pull_screenshots"
    def adb = "adb"
    def testApkTarget = ":packageDebugAndroidTest"
}

class ScreenshotsPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.extensions.create("screenshots", ScreenshotsPluginExtension)

    def depTarget = project.screenshots.testApkTarget
    project.task('pullScreenshots', dependsOn: depTarget) << {
      def output = project.tasks.getByPath(depTarget).getOutputs().getFiles().getSingleFile().getAbsolutePath()
      println output
      project.exec {
        executable = project.screenshots.bin
        args = ["--apk", output.toString()]
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
  }
}
