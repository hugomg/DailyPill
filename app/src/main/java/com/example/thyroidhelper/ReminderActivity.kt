package com.example.thyroidhelper

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager

class ReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        setTitle(R.string.reminder_title)

        if (Build.VERSION.SDK_INT < 27) {
            this.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        } else {
            setShowWhenLocked(true)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun performUpdateTime(btn: View) {
        DataModel.setDrugTakenTimestamp(this, DataModel.currentTimestamp())
        finish()
    }
}
