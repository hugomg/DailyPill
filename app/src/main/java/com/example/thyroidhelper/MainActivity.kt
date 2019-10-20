package com.example.thyroidhelper

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.SharedPreferences
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

    lateinit var preferences: SharedPreferences;
    lateinit var dateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = this.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        dateTextView = findViewById(R.id.date)

        // Clear preferences
        //preferences.edit()
        //    .clear()
        //    .apply()
    }

    override fun onResume() {
        redraw()
        super.onResume()
    }

    private fun redraw() {
        val timestamp = preferences.getLong(PREFS_DATE_KEY, 0)
        val lastClick = Calendar.getInstance()
        lastClick.setTimeInMillis(timestamp)

        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (lastClick.before(today)) {
            dateTextView.text = "---"
        } else {
            dateTextView.text =
                DateFormat.getTimeInstance(DateFormat.SHORT).format(lastClick.getTime())
        }
    }


    fun updateTime(btn: View) {
        val timestamp = Calendar.getInstance().getTimeInMillis()

        val ok = preferences.edit()
            .putLong(PREFS_DATE_KEY, timestamp)
            .commit()

        if (ok) {
            redraw()
        } else {
            // TODO: show error toast
        }
    }
}
