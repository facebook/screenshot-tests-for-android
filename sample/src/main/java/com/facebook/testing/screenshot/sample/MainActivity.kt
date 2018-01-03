/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.testing.screenshot.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.facebook.litho.LithoView

class MainActivity : AppCompatActivity() {
  companion object {
    private const val STATUS = "status"

    fun intent(status: Status) = Intent().apply {
      putExtra(STATUS, status.name)
    }
  }

  enum class Status {
    OK,
    WARNING,
    ERROR
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(findViewById(R.id.toolbar))

    val textView = findViewById<TextView>(R.id.text_view)
    val status = Status.valueOf(intent.string(STATUS, Status.OK.name))
    when (status) {
      Status.OK -> textView.run {
        setTextColor(context.color(R.color.ok))
        text = "Status is OK"
      }
      Status.WARNING -> textView.run {
        setTextColor(context.color(R.color.warning))
        text = "Status is WARNING"
      }
      Status.ERROR -> textView.run {
        setTextColor(context.color(R.color.error))
        text = "Status is ERROR"
      }
    }

    findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
      Snackbar.make(view, "This is a snackbar", Snackbar.LENGTH_LONG)
          .setAction("Action", null)
          .show()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  private fun Intent.string(name: String, defValue: String): String {
    if (hasExtra(name)) {
      return getStringExtra(name)
    }
    return defValue
  }

  private fun Context.color(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
  }
}
