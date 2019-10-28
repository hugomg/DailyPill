package com.example.thyroidhelper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import android.os.SystemClock
import java.util.*


class MainActivity : AppCompatActivity() {

    private var resetMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        resetMenuItem = menu.findItem(R.id.action_reset)
        resetMenuItem?.isEnabled = hasTakenDrugToday(this)
        return true
    }

    override fun onResume() {
        if (hasTakenDrugToday(this)) {
            gotoDrugTaken(false)
        } else {
            gotoDrugNotTaken(false)
        }
        super.onResume()
    }

    private fun switchFragment(useFade: Boolean, fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            if (useFade) { setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) }
            commit()
        }
    }

    private fun gotoDrugTaken(useFade: Boolean) {
        resetMenuItem?.isEnabled = true
        switchFragment(useFade, MedicineTakenFragment())
    }

    private fun gotoDrugNotTaken(useFade: Boolean) {
        resetMenuItem?.isEnabled = false
        switchFragment(useFade, MedicineNotTakenFragment())
    }

    @Suppress("UNUSED_PARAMETER")
    fun performUpdateTime(btn: View) {
        setDrugTakenTime(this, currentTime())
        gotoDrugTaken(true)
    }

    private fun doReset() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.reset_confirmation_title)
            .setMessage(R.string.reset_confirmation_message)
            .setPositiveButton(R.string.reset_confirmation_ok) { _, _ ->
                unsetDrugTakenTime(this)
                gotoDrugNotTaken(false)
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
        sendMorningReminderNotification(this)
    }

    private fun doAddAlarm() {
        val alarm_hour   = 4
        val alarm_minute = 0

        val now = Calendar.getInstance()

        val timeToday = Calendar.getInstance()
        timeToday.set(Calendar.HOUR_OF_DAY, alarm_hour)
        timeToday.set(Calendar.MINUTE,      alarm_minute)
        timeToday.set(Calendar.SECOND, 0)
        timeToday.set(Calendar.MILLISECOND, 0)

        val timeTomorrow = Calendar.getInstance()
        timeTomorrow.add(Calendar.DATE, 1)
        timeTomorrow.set(Calendar.HOUR_OF_DAY, alarm_hour)
        timeTomorrow.set(Calendar.MINUTE,      alarm_minute)
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
        }
        // Fallback
        return super.onOptionsItemSelected(item)
    }
}
