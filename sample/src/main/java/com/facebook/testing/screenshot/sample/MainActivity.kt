/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.testing.screenshot.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.ColorRes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
