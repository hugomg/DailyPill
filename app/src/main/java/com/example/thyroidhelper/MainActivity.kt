package com.example.thyroidhelper

import android.content.Intent
import android.content.SharedPreferences
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import android.view.*
import android.widget.Button
import android.widget.TextView

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
            updateUI(true)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI(false)
        DataModel.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        DataModel.removeListener(this)
    }

    private fun updateUI(useFade: Boolean) {
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

    private fun doReset() {
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
                doReset()
                return true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
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
            button.setOnClickListener(this::performUpdateTime)
            return root
        }

        @Suppress("UNUSED_PARAMETER")
        private fun performUpdateTime(btn: View) {
            DataModel.takeDrugNow()
        }
    }

    class MedicineTakenFragment : Fragment() {

        private lateinit var drugTakenMessage: String
        private lateinit var reminderEnabledMessage: CharSequence
        private lateinit var reminderDisabledMessage: CharSequence

        private lateinit var drugTakenMessageView: TextView
        private lateinit var reminderStatus: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            drugTakenMessage =  getString(R.string.drug_taken_message)
            reminderEnabledMessage = getText(R.string.reminders_are_enabled)
            reminderDisabledMessage = getText(R.string.reminders_are_disabled)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val root = inflater.inflate(R.layout.fragment_medicine_taken, container, false)
            drugTakenMessageView = root.findViewById(R.id.drug_taken_message)
            reminderStatus = root.findViewById(R.id.reminder_status)

            reminderStatus.setOnClickListener(this::clickReminderStatus)
            return root
        }

        override fun onResume() {
            super.onResume()

            // Use non-breaking space to avoid a line-break between 6:00 and AM
            val timestamp = DataModel.getDrugTakenTimestamp()
            val timeStr = DateFormat.getTimeFormat(activity).format(timestamp)
            val nbspTimeStr = timeStr.replace(" ", "\u00A0" )
            drugTakenMessageView.text = String.format(drugTakenMessage, nbspTimeStr)

            if (DataModel.reminderIsEnabled()) {
                reminderStatus.text = reminderEnabledMessage
            } else {
                reminderStatus.text = reminderDisabledMessage
            }
        }

        private fun clickReminderStatus(v: View) {
            val intent = Intent(activity!!, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
