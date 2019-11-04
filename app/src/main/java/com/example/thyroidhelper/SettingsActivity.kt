package com.example.thyroidhelper

import android.os.Bundle
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp() : Boolean{
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val enabledSummary = activity!!.getString(R.string.morning_reminder_enabled_summary)
            val disabledSummaty = activity!!.getString(R.string.morning_reminder_disabled_summary)

            val morningReminderTime :TimePreference = findPreference("morning_reminder_time")!!
            morningReminderTime.summaryProvider = Preference.SummaryProvider { v: Preference ->
                if (DataModel.reminderIsEnabled()) {
                    val cal = DataModel.morningReminderTimeForTheSameDayAs(Calendar.getInstance())
                    val timeStr = DateFormat.getTimeFormat(activity).format(cal.time)
                    String.format(enabledSummary, timeStr)
                } else {
                    disabledSummaty
                }
            }
        }

        // The preference library has a boneheaded and inextensible design so we need to override
        // this function and copy paste some code from the original implementations.
        override fun onDisplayPreferenceDialog(preference: Preference?) {
            if (preference is TimePreference) {
                val fragment = TimePreferenceDialogFragmentCompat()
                val bundle = Bundle(1); bundle.putString("key", preference.getKey())
                fragment.setArguments(bundle)
                fragment.setTargetFragment(this, 0)
                fragment.show(fragmentManager!!, null)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

}