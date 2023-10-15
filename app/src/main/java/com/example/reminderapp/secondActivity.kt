package com.example.reminderapp

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.reminderapp.databinding.ActivitySecondBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Data


var daysArray = arrayOf("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY")


val currentReminderID = mutableListOf<Int>()

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = intent?.getIntExtra("NOTIFICATION_ID", 0) ?: 0
        val channelId = intent?.getStringExtra("CHANNEL_ID") ?: ""

        val reminderTitle = vb.nameField.text.toString()

        val builder = NotificationCompat.Builder(context!!, channelId)
            .setSmallIcon(R.drawable.med_icon)
            .setContentTitle("$reminderTitle - Reminder")
            .setContentText("Don't Forget!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Handle permission not granted
                return
            }
        }

        notificationManager.notify(notificationId, builder.build())
    }
}
private lateinit var vb: ActivitySecondBinding
class secondActivity : AppCompatActivity() {


    fun scheduleNotificationCancellation(notificationId: Int) {
        val inputData = Data.Builder()
            .putInt("NOTIFICATION_ID", notificationId)
            .putString("CHANNEL_ID", "channel_id_reminder")
            .putBoolean("CANCEL_NOTIFICATION", true)
            .build()

        val reminderWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(reminderWorkRequest)
    }


    private val CHANNEL_ID = "channel_id_reminder"



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun sendNotificationWithAlarm(daysList: MutableList<String>, timeReceived: String) {
        val notificationId = 0
        val channelId = CHANNEL_ID

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationBroadcastReceiver::class.java)
        intent.putExtra("NOTIFICATION_ID", notificationId)
        intent.putExtra("CHANNEL_ID", channelId)

        for (day in daysList) {
            val dayOfWeek = getDayOfWeek(day)
            if (dayOfWeek != -1) {
                val alarmTime = calculateAlarmTime(dayOfWeek, timeReceived)

                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    dayOfWeek,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
            }
        }
    }

    private fun getDayOfWeek(day: String): Int {
        return when (day.toUpperCase()) {
            "SUNDAY" -> Calendar.SUNDAY
            "MONDAY" -> Calendar.MONDAY
            "TUESDAY" -> Calendar.TUESDAY
            "WEDNESDAY" -> Calendar.WEDNESDAY
            "THURSDAY" -> Calendar.THURSDAY
            "FRIDAY" -> Calendar.FRIDAY
            "SATURDAY" -> Calendar.SATURDAY
            else -> -1
        }
    }

    private fun calculateAlarmTime(dayOfWeek: Int, time: String): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)

        val timeParts = time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    private fun populateDetails(reminder: Reminder) {
        uncheckDays(reminder.checkedDays)
        vb.nameField.setText(reminder.title)
        vb.dosageField.setText(reminder.dosage)

        val reminderTimeParts = reminder.time.split(":")
        var reminderHour = reminderTimeParts[0].toInt()
        val reminderMinutes = reminderTimeParts[1].toInt()

        if (!reminder.isAM && reminderHour < 12) {
            reminderHour += 12
        }

        vb.timePicker.currentHour = reminderHour
        vb.timePicker.currentMinute = reminderMinutes
    }

    private fun uncheckDays(checkedList: MutableList<String>) {
        val checkBoxes = daysArray
        for (dayName in checkBoxes) {
            val checkBoxID = resources.getIdentifier(dayName, "id", packageName)
            val dayCheck = findViewById<CheckBox>(checkBoxID)
            if (dayCheck.isChecked) {
                dayCheck.toggle()
            }
        }
        recheckDays(checkedList)
    }

    private fun recheckDays(daysList: MutableList<String>) {
        for (dayName in daysList) {
            val checkBoxID = resources.getIdentifier(dayName, "id", packageName)
            val dayCheck = findViewById<CheckBox>(checkBoxID)
            dayCheck.isChecked = true
        }
    }

    private fun updateReminder(existingReminder: Reminder) {
        existingReminder.title = vb.nameField.text.toString()
        existingReminder.dosage = vb.dosageField.text.toString()
        existingReminder.checkedDays

        val currentDays = checkedDays()
        existingReminder.checkedDays = currentDays
        val hour = vb.timePicker.currentHour
        val minute = vb.timePicker.currentMinute

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        val formattedHour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
        val formattedMinute = String.format("%02d", calendar.get(Calendar.MINUTE))

        val formattedTime = "$formattedHour:$formattedMinute"

        existingReminder.time = formattedTime
        existingReminder.isAM = AM_Checker(hour)

        MainActivity.reminderArray[existingReminder.ID] = existingReminder
        ReminderStorage.saveReminders(this, MainActivity.reminderArray)

        finish()
    }

    private fun checkedDays(): MutableList<String> {
        val checkBoxes = daysArray
        var checkedDaysList = mutableListOf<String>()
        for (dayName in checkBoxes) {
            val checkBoxID = resources.getIdentifier(dayName, "id", packageName)
            val dayCheck = findViewById<CheckBox>(checkBoxID)
            if (dayCheck.isChecked) {
                checkedDaysList.add(dayName)
            }
        }
        return checkedDaysList
    }

    private fun AM_Checker(hourOfDay: Int): Boolean {
        return hourOfDay in 0 until 12
    }

    private fun create_active(view: View, reminderID: Int) {
        var hour = vb.timePicker.currentHour
        val minute = vb.timePicker.currentMinute

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        if (hour == 12 && AM_Checker(hour)) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
        }


        val formattedHour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
        val formattedMinute = String.format("%02d", calendar.get(Calendar.MINUTE))

        val formattedTime = "$formattedHour:$formattedMinute"
        val isAM = AM_Checker(hour)

        val remID = MainActivity.reminderArray.size

        if (vb.nameField.text.isNullOrEmpty()) {
            Snackbar.make(view, "Please enter reminder name!", Snackbar.LENGTH_LONG).show()
        } else {
            val currentReminder = Reminder(
                vb.nameField.text.toString(),
                vb.dosageField.text.toString(),
                formattedTime,
                isAM,
                remID,
                checkedDays(),
                reminderID
            )
            MainActivity.remindersNum += 1
            MainActivity.totalReminders = MainActivity.reminderArray.size

            MainActivity.reminderArray.add(currentReminder)
            ReminderStorage.saveReminders(this, MainActivity.reminderArray)


            sendNotificationWithAlarm(checkedDays(), formattedTime)

            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(vb.root)

        createNotificationChannel()

        vb.backButton.setOnClickListener {
            finish()
        }

        fun generateUniqueReminderId(): Int {
            ReminderStorage.incrementReminderCounter(this)
            return ReminderStorage.getReminderCounter(this)
        }
        val fetchReminderID = generateUniqueReminderId()
        currentReminderID.add(0)
        currentReminderID[0] = fetchReminderID

        val reminderToEdit = intent.getSerializableExtra("EDIT_REMINDER") as? Reminder
        if (reminderToEdit != null) {
            populateDetails(reminderToEdit)
            vb.createButton.setOnClickListener {
                updateReminder(reminderToEdit)
            }
        } else {
            vb.createButton.setOnClickListener {
                create_active(it, fetchReminderID)
            }
        }
    }
}