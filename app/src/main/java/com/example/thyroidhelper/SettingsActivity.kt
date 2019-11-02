package com.example.thyroidhelper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.text.DateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
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
            val medicationTime :TimePreference = findPreference("morning_reminder_time")!!
            medicationTime.summaryProvider = MedicationTimeSummaryProvider()
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

    class MedicationTimeSummaryProvider: Preference.SummaryProvider<TimePreference> {
        override fun provideSummary(preference: TimePreference): String {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, preference.hour)
            calendar.set(Calendar.MINUTE, preference.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.time)
        }
    }
}