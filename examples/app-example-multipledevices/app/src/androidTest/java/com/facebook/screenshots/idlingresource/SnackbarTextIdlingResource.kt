package com.facebook.screenshots.idlingresource

import android.app.Activity
import android.support.test.espresso.IdlingResource
import android.widget.TextView
import com.facebook.screenshots.R

class SnackbarTextIdlingResource(private val activity: Activity, private val textId: Int) : IdlingResource {

    override fun getName(): String = "SnackbarTextIdlingResource"

    override fun isIdleNow(): Boolean {
        val textView = activity.findViewById<TextView>(R.id.snackbar_text)
        return textView?.text == activity.resources.getString(textId) ?: false
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {}
}