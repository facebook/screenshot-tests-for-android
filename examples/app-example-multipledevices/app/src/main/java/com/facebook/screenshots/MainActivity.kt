package com.facebook.screenshots

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, R.string.hi_text, Snackbar.LENGTH_LONG).show()
        }

        intent?.let {
            val messageType = it.getIntExtra(MESSAGE_TYPE_KEY, 0)
            when (messageType) {
                MessageType.WARNING -> default_text.setTextColor(ContextCompat.getColor(this, R.color.warning))
                MessageType.ERROR -> default_text.setTextColor(ContextCompat.getColor(this, R.color.error))
                MessageType.SUCCESS -> default_text.setTextColor(ContextCompat.getColor(this, R.color.ok))
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
