package com.example.thyroidhelper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import android.view.*
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat
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
        resetMenuItem!!.isEnabled = DataModel.hasTakenDrugToday()
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

    override fun onStop() {
        super.onStop()
        DataModel.removeListener(this)
    }

    private fun updateUI(useFade: Boolean) {
        val drugTaken = DataModel.hasTakenDrugToday()
        val fragment =
            if (drugTaken) { MedicineTakenFragment()    }
            else           { MedicineNotTakenFragment() }

        resetMenuItem?.isEnabled = drugTaken
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            if (useFade) {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
            commit()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun performUpdateTime(btn: View) {
        DataModel.setDrugTakenTimestamp(DataModel.currentTimestamp())
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

    private fun doReminder() {
        val intent = Intent(this, ReminderActivity::class.java)
        startActivity(intent)
    }

    private fun doAddNotification() {
        Notifications.sendMorningReminderNotification()
    }

    private fun doAddAlarm() {
        val alarmHour   = DataModel.getMedicationTimeHours()
        val alarmMinute = DataModel.getMedicationTimeMinutes()

        val now = Calendar.getInstance()

        val timeToday = Calendar.getInstance()
        timeToday.set(Calendar.HOUR_OF_DAY, alarmHour)
        timeToday.set(Calendar.MINUTE,      alarmMinute)
        timeToday.set(Calendar.SECOND, 0)
        timeToday.set(Calendar.MILLISECOND, 0)

        val timeTomorrow = Calendar.getInstance()
        timeTomorrow.add(Calendar.DATE, 1)
        timeTomorrow.set(Calendar.HOUR_OF_DAY, alarmHour)
        timeTomorrow.set(Calendar.MINUTE,      alarmMinute)
        timeTomorrow.set(Calendar.SECOND, 0)
        timeTomorrow.set(Calendar.MILLISECOND, 0)

        val nextTime = if (now.before(timeToday)) { timeToday } else { timeTomorrow }

        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent)

        val toast_msg = String.format("#Set alarm for %02d:%02d", alarmHour, alarmMinute)
        val toast = Toast.makeText(this, toast_msg, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                doReset()
                return true
            }
            R.id.action_reminder -> {
                doReminder()
                return true
            }
            R.id.action_register_notification -> {
                doAddNotification()
                return true
            }
            R.id.action_register_alarm -> {
                doAddAlarm()
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

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_medicine_not_taken, container, false)
        }
    }

    class MedicineTakenFragment : Fragment() {

        private lateinit var drugTakenMessage: String
        private lateinit var drugTakenMessageView: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            drugTakenMessage =  getString(R.string.drug_taken_message)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val root = inflater.inflate(R.layout.fragment_medicine_taken, container, false)
            drugTakenMessageView = root.findViewById(R.id.drug_taken_message)
            return root
        }

        override fun onResume() {
            val timestamp = DataModel.getDrugTakenTimestamp()
            val timeStr = DateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp)
            // Use non-breaking space to avoid a line-break between 6:00 and AM
            val nbspTimeStr = timeStr.replace(" ", "\u00A0" )
            drugTakenMessageView.text = String.format(drugTakenMessage, nbspTimeStr)

            super.onResume()
        }

    }
}
