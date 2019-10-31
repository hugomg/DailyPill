package com.example.thyroidhelper

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

class ReminderActivity : AppCompatActivity(), SharedPreferencesListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        setTitle(R.string.reminder_title)

        if (Build.VERSION.SDK_INT < 27) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        } else {
            setShowWhenLocked(true)
        }

        // Force the button to use the whole available width. By default the width is set to
        // WRAP_CONTENT, and apparently only changing it here works. Setting the layout size on the
        // XML file is not enough.
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onResume() {
        super.onResume()
        checkIfFinished()
        DataModel.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        DataModel.removeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(DataModel.DRUG_TAKEN_TIMESTAMP)) {
            checkIfFinished()
        }
    }

    private fun checkIfFinished() {
        if (DataModel.hasTakenDrugToday()) {
            finish()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun performUpdateTime(btn: View) {
        DataModel.setDrugTakenTimestamp(DataModel.currentTimestamp())
    }
}