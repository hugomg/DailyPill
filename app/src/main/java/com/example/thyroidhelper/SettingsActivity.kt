package com.example.thyroidhelper

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment

import java.util.*

class SettingsActivity : AppCompatActivity(), SharedPreferencesListener {

    private lateinit var morningReminderLabelArea: View
    private lateinit var morningReminderSwitch: Switch
    private lateinit var morningReminderSummary: TextView

    private lateinit var morningReminderEnabledSummary: String
    private lateinit var morningReminderDisabledSummary: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        morningReminderLabelArea = findViewById(R.id.morning_reminder_label_area)
        morningReminderSwitch = findViewById(R.id.morning_reminder_switch)
        morningReminderSummary = findViewById(R.id.morning_reminder_summary)

        morningReminderEnabledSummary = getString(R.string.morning_reminder_enabled_summary)
        morningReminderDisabledSummary = getString(R.string.morning_reminder_disabled_summary)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp() : Boolean{
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        updateMorningReminderLabelArea()
        updateMorningReminderSummary()
        updateMorningReminderSwitch()
        DataModel.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        DataModel.removeListener(this)
    }

    fun morningReminderLabelClick(v: View) {
        Log.d("TEST", v.isClickable.toString())
        TimePickerFragment().show(supportFragmentManager, "timePicker")
    }

    fun morningReminderToggle(v: View) {
        val switch = v as Switch
        DataModel.setReminderIsEnabled(switch.isChecked)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(DataModel.MORNING_REMINDER_ENABLED)) {
            updateMorningReminderLabelArea()
        }

        if (key.equals(DataModel.MORNING_REMINDER_ENABLED)
            || key.equals(DataModel.MORNING_REMINDER_TIME)) {
            updateMorningReminderSummary()
        }

        if (key.equals(DataModel.MORNING_REMINDER_ENABLED)) {
            updateMorningReminderSwitch()
        }
    }

    private fun updateMorningReminderLabelArea() {
        morningReminderLabelArea.isClickable = DataModel.reminderIsEnabled()
    }

    private fun updateMorningReminderSummary() {
        if (DataModel.reminderIsEnabled()) {
            val cal = DataModel.morningReminderTimeForTheSameDayAs(Calendar.getInstance())
            val timeStr = DateFormat.getTimeFormat(this).format(cal.time)
            val text = String.format(morningReminderEnabledSummary, timeStr)
            morningReminderSummary.text = text
        } else {
            morningReminderSummary.text = morningReminderDisabledSummary        }
    }

    private fun updateMorningReminderSwitch() {
        morningReminderSwitch.isChecked = DataModel.reminderIsEnabled()
    }
}

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val (hour, minute) = DataModel.getMorningReminderTime()
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        DataModel.setMorningReminderTime(hourOfDay, minute)
    }
}
