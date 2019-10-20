package com.example.thyroidhelper

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.SharedPreferences
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import java.text.DateFormat
import java.util.*

//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//import java.time.format.FormatStyle

const val PREFS_FILE_NAME = "com.example.thyroidhelper.preferences"
const val PREFS_DATE_KEY = "last_date"

class MainActivity : AppCompatActivity() {

    lateinit var preferences: SharedPreferences
    lateinit var dateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        preferences = this.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        dateTextView = findViewById(R.id.date)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onResume() {
        redraw()
        super.onResume()
    }

    private fun redraw() {
        val timestamp = preferences.getLong(PREFS_DATE_KEY, 0)
        val lastClick = Calendar.getInstance()
        lastClick.timeInMillis = timestamp

        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        if (lastClick.before(today)) {
            dateTextView.text = "---"
        } else {
            dateTextView.text =
                DateFormat.getTimeInstance(DateFormat.SHORT).format(lastClick.time)
        }
    }

    // Menu action
    private fun forgetLastTime() {
        preferences.edit()
            .remove(PREFS_DATE_KEY)
            .apply()
        redraw()
    }


    // Onclick handler for the button
    fun updateTime(btn: View) {
        val timestamp = Calendar.getInstance().timeInMillis
        preferences.edit()
            .putLong(PREFS_DATE_KEY, timestamp)
            .apply()
        redraw()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                forgetLastTime()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

}
