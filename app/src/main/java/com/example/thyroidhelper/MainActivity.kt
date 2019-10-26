package com.example.thyroidhelper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import android.app.PendingIntent
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



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

    private fun performReset() {
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

    private fun performReminder() {
        val intent = Intent(this, ReminderActivity::class.java)
        startActivity(intent)
    }

    fun performRegisterNotification() {

        val CHANNEL_ID = "channel_01"

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "#Test"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = "#This is a test"

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, ReminderActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // Send the notification
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("My notification")
            .setContentText("Hello World")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)

        val NOTIFICATION_ID = 0
        with (NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                performReset()
                return true
            }
            R.id.action_reminder -> {
                performReminder()
                return true
            }
            R.id.action_register_notification -> {
                performRegisterNotification()
                return true
            }
        }
        // Fallback
        return super.onOptionsItemSelected(item)
    }
}
