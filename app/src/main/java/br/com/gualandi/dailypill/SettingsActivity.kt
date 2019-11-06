package br.com.gualandi.dailypill

import android.content.Context
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

            val reminderTime :TimePreference = findPreference("reminder_time")!!
            reminderTime.summaryProvider = ReminderTimeSummaryProvider(activity!!)
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

    class ReminderTimeSummaryProvider(private val ctx: Context): Preference.SummaryProvider<TimePreference> {
        override fun provideSummary(preference: TimePreference): String {
            val now = Calendar.getInstance()
            val cal = DataModel.dailyReminderTimeForTheSameDayAs(now)
            return DateFormat.getTimeFormat(ctx).format(cal.time)
        }
    }
}