/*
 * Copyright © 2019 Hugo Musso Gualandi.
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    override fun onSupportNavigateUp() : Boolean{
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val reminderTime : TimePreference = findPreference("reminder_time")!!
            reminderTime.summaryProvider = ReminderTimeSummaryProvider(activity!!)

            val testButton : Preference = findPreference("test_reminder")!!
            testButton.setOnPreferenceClickListener({ clickTestButton(); true  })
        }

        // The preference library has a boneheaded and inextensible design so we need to override
        // this function and copy paste some code from the original implementations.
        override fun onDisplayPreferenceDialog(preference: Preference?) {
            if (preference is TimePreference) {
                val bundle = Bundle(1); bundle.putString("key", preference.getKey())
                val fragment = TimePreferenceDialogFragmentCompat()
                fragment.setArguments(bundle)
                fragment.setTargetFragment(this, 0)
                fragment.show(fragmentManager!!, null)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }

        private fun clickTestButton() {
            val now = Calendar.getInstance()
            Notifications.sendReminderNotification(now, true)
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