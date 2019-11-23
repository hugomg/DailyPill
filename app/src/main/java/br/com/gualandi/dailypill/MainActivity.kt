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

import android.content.Intent
import android.content.SharedPreferences
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import android.view.*
import android.widget.Button
import android.widget.TextView
import java.util.*

class MainActivity : AppCompatActivity(), SharedPreferencesListener {

    private var resetMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        resetMenuItem = menu.findItem(R.id.action_reset)
        resetMenuItem!!.isVisible = DataModel.hasTakenDrugToday()
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(DataModel.DRUG_TAKEN_TIMESTAMP)) {
            updateBody(true)
        }
    }

    override fun onStart() {
        super.onStart()
        updateBody(false)
        DataModel.addListener(this)
    }

    override fun onResume() {
        super.onResume()
        checkForMissedAlarms()
    }

    override fun onStop() {
        super.onStop()
        DataModel.removeListener(this)
    }

    private fun updateBody(useFade: Boolean) {
        val drugTaken = DataModel.hasTakenDrugToday()
        val fragment =
            if (drugTaken) { MedicineTakenFragment()    }
            else           { MedicineNotTakenFragment() }

        resetMenuItem?.isVisible = drugTaken
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            if (useFade) {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
            commit()
        }
    }

    private fun checkForMissedAlarms() {
        // 1) Are reminders enabled?
        if (!DataModel.reminderIsEnabled()) { return }

        // 2) Are we past the expected time for the next reminder?
        // Leave some wiggle room for AlarmManager delay. Watch out for integer overflow.
        val nowTimestamp = Calendar.getInstance().timeInMillis
        val nextAlarmTimestamp = DataModel.getNextAlarmTimestamp()
        if (nowTimestamp - 1*60*1000L < nextAlarmTimestamp) { return }

        // 3) Have we only recently turned on the phone?
        // The AlarmInitReceiver might not have run yet. Leave some extra large wiggle room
        // because we can only measure time since boot, not time since the user unlocked the phone.
        val timeSinceBoot = SystemClock.elapsedRealtime()
        if (timeSinceBoot < 5*60*1000L) { return }

        // 4) Oh well, it seems like something broke our AlarmManager alarm ¯\_(ツ)_/¯
        AlertDialog.Builder(this)
            .setTitle(R.string.missed_reminder_title)
            .setIcon(R.drawable.ic_pill)
            .setMessage(R.string.missed_reminder_message)
            .setPositiveButton(R.string.missed_reminder_button) { _, _ -> resetAlarms() }
            .setOnDismissListener { resetAlarms() }
            .show()
    }

    private fun resetAlarms() {
        Notifications.resetAlarm()
    }

    private fun resetMedicineTakenTime() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.reset_confirmation_title)
            .setMessage(R.string.reset_confirmation_message)
            .setPositiveButton(R.string.reset_confirmation_ok) { _, _ ->
                DataModel.unsetDrugTakenTimestamp()
            }
            .setNegativeButton(R.string.reset_confirmation_cancel,null)
            .create()
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                resetMedicineTakenTime()
                return true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
        }
        // Fallback
        return super.onOptionsItemSelected(item)
    }

    class MedicineNotTakenFragment : Fragment() {

        private lateinit var button: Button

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val root = inflater.inflate(R.layout.fragment_medicine_not_taken, container, false)
            button = root.findViewById(R.id.button)
            button.setOnClickListener { clickButton() }
            return root
        }

        private fun clickButton() {
            DataModel.takeDrugNow()
        }
    }

    class MedicineTakenFragment : Fragment() {

        private lateinit var drugTakenMessageView: TextView
        private lateinit var bannerView: View
        private lateinit var bannerButton: Button

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val root = inflater.inflate(R.layout.fragment_medicine_taken, container, false)
            drugTakenMessageView = root.findViewById(R.id.drug_taken_message)
            bannerView = root.findViewById(R.id.banner)
            bannerButton = root.findViewById(R.id.banner_button)

            bannerButton.setOnClickListener { gotoSettings() }
            return root
        }

        override fun onResume() {
            super.onResume()

            // The non-breaking space avoids a line-break between 6:00 and AM
            val timestamp = DataModel.getDrugTakenTimestamp()
            val timeStr = DateFormat.getTimeFormat(activity).format(timestamp)
            val nbspTimeStr = timeStr.replace(" ", "\u00A0" )

            val drugTakenMessage = getString(R.string.drug_taken_message)
            drugTakenMessageView.text = String.format(drugTakenMessage, nbspTimeStr)

            bannerView.visibility =
                if (DataModel.reminderIsEnabled()) { View.GONE } else { View.VISIBLE }
        }

        private fun gotoSettings() {
            val intent = Intent(activity!!, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
