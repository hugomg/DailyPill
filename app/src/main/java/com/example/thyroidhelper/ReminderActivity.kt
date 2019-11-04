package com.example.thyroidhelper

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button

class ReminderActivity : AppCompatActivity(), SharedPreferencesListener {

    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        setTitle(R.string.reminder_title)

        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
        } else @Suppress("DEPRECATION") {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }

        // Force the button to use the whole available width. By default the width is set to
        // WRAP_CONTENT, and apparently only changing it here works. Setting the layout size on the
        // XML file is not enough.
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        button = findViewById(R.id.button)!!
        button.setOnClickListener(this::performUpdateTime)
    }

    override fun onResume() {
        super.onResume()
        checkIfFinished()
        DataModel.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        DataModel.removeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(DataModel.DRUG_TAKEN_TIMESTAMP)) {
            checkIfFinished()
        }
    }

    private fun checkIfFinished() {
        Log.d("Test", "ONCLICK IS RUNNING")
        if (DataModel.hasTakenDrugToday()) {
            finish()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun performUpdateTime(btn: View) {
        DataModel.takeDrugNow()
    }
}