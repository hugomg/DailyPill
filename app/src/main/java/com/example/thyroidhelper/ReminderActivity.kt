package com.example.thyroidhelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class ReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        setTitle("Daily Reminder")
    }

    @Suppress("UNUSED_PARAMETER")
    fun performUpdateTime(btn: View) {
        setDrugTakenTime(this, currentTime())
        finish()
    }
}
