package com.facebook.screenshots

import android.os.Bundle
import android.support.test.runner.AndroidJUnitRunner
import com.facebook.testing.screenshot.ScreenshotRunner

class ExampleScreenshotRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle?) {
        ScreenshotRunner.onCreate(this, arguments)
        super.onCreate(arguments)
    }

    override fun finish(resultCode: Int, results: Bundle?) {
        ScreenshotRunner.onDestroy()
        super.finish(resultCode, results)
    }
}