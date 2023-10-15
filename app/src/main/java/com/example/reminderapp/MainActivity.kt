package com.example.reminderapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.reminderapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale


var hoursUntilAlarmStorage = mutableListOf<Int>()
var daysStringArray = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
val daysMap = mapOf(0 to "SUNDAY",
    1 to "MONDAY",
    2 to "TUESDAY",
    3 to "WEDNESDAY",
    4 to "THURSDAY",
    5 to "FRIDAY",
    6 to "SATURDAY")

class MainActivity : AppCompatActivity() {

    private val handler: Handler = Handler()
    private val refreshRunnable: Runnable = object : Runnable {
        override fun run() {
            create_reminder_view()
            handler.postDelayed(this, 60000)
        }
    }

    private lateinit var vb: ActivityMainBinding

    companion object {
        var remindersNum = 0
        var totalReminders: Int? = null
        val reminderArray = mutableListOf<Reminder>()
    }

    fun update_reminders_count() {
        if(reminderArray.size > 1) {
            vb.remindersText.text = getString(R.string.reminder_status, reminderArray.size.toString())
        } else if(reminderArray.size == 1) {
            vb.remindersText.text = getString(R.string.reminder_status_single, "1")
        } else {
            vb.remindersText.text = getString(R.string.reminder_status, "No")
            vb.nextAlarmIn.text = ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        handleButtonClicks(vb, this)
        reminderArray.clear()
        reminderArray.addAll(ReminderStorage.getReminders(this))
        update_reminders_count()

        create_reminder_view()
    }

    private fun create_reminder_view() {

        vb.addReminderText.text = "Add Reminder"
        val parentLayout: LinearLayout = findViewById(R.id.reminderLayout)
        parentLayout.removeAllViews()

        val reminders = ReminderStorage.getReminders(this)

        if (reminders.isNotEmpty()) {
            vb.addReminderText.text = ""
            handler.postDelayed(refreshRunnable, 10000)
            hoursUntilAlarmStorage.clear()
        }
        for (reminder in reminders) {
            val inflater = LayoutInflater.from(this)
            val reminderItemView = inflater.inflate(R.layout.reminder_item, parentLayout, false)

            val reminderTitle = reminderItemView.findViewById<TextView>(R.id.reminderTitle)
            val reminderDosage = reminderItemView.findViewById<TextView>(R.id.reminderDosage)
            val reminderTime = reminderItemView.findViewById<TextView>(R.id.reminderTime)
            val deleteButton = reminderItemView.findViewById<ImageButton>(R.id.deleteBtn)
            val editButton = reminderItemView.findViewById<ImageButton>(R.id.editBtn)
            val reminderDays = reminderItemView.findViewById<TextView>(R.id.reminderDays)

            val secondActivityContainer = secondActivity()
            val reminderID = reminder.reminderID

            deleteButton.setOnClickListener {
                ReminderStorage.clearReminder(this, reminder.reminderID)
                hoursUntilAlarmStorage.clear()
                deleteReminder(reminder, secondActivityContainer, reminderID)
            }

            editButton.setOnClickListener {
                val intent = Intent(this@MainActivity, secondActivity::class.java)
                intent.putExtra("EDIT_REMINDER", reminder)
                startActivity(intent)
            }


            var index = 0
            val dayContainer = mutableListOf<Int>()
            val dayList = mutableListOf<String>()

            while(index <= 6) {
                if(daysMap[index] in reminder.checkedDays) {
                    dayContainer.add(index)
                    val currentDay = LocalDate.now().getDayOfWeek()
                    index ++
                } else {
                    index ++
                }
            }

            for(item in dayContainer) {
                dayList.add(daysStringArray[item])
            }
            val dayString = dayList.toString()
            val dayStringFormatted = dayString.trim('[', ']')

            reminderDays.text = getString(R.string.reminder_days, dayStringFormatted)


            val reminderNextAlarm = reminderItemView.findViewById<TextView>(R.id.reminderNextAlarm)

            reminderTitle.text = reminder.title
            reminderDosage.text = getString(R.string.reminder_dosage, reminder.dosage)

            val timeContainer = timeConverter()

            val daysChecked = reminder.checkedDays
            timeContainer.days_until(daysChecked, false)

            val reminder_time = timeContainer.convert_time(reminder.time, reminder.isAM)
            val current_time = timeContainer.current_time()
            val time_until_formatted = timeContainer.time_until_formatted(reminder_time, current_time, false, reminder.isAM, daysChecked)

            fun AM_Checker(): String {
                if(reminder.isAM) {
                    return " A.M"
                } else {
                    return " P.M."
                }
            }

            var formattedHour = (reminder.time.take(2)).toInt()
            var formattedMinutes = (reminder.time.takeLast(2)).toInt()

            if (formattedHour > 12) {
                formattedHour -= 12
            } else if (formattedHour == 0) {
                formattedHour = 12
            }

            val minuteString = if(formattedMinutes.toString().length < 2) "0$formattedMinutes" else formattedMinutes.toString()

            var timeString = "$formattedHour:$minuteString"


            reminderTime.text = getString(R.string.reminder_time, (timeString + AM_Checker()))
            reminderNextAlarm.text = getString(R.string.alarm_time, time_until_formatted)

            fun convertNextAlarmIn(time: Int) {
                if (time.toString().length <= 2) {
                    val alarmString = "$time minutes"
                    vb.nextAlarmIn.text = getString(R.string.upcoming_alarm, alarmString)
                } else {
                    var plural = ""
                    val stringHours = time.toString().first()
                    val stringMinutes = time.toString().takeLast(2)
                    if (stringHours != '1') {
                        plural = "s"
                    }
                    val alarmString = "$stringHours hour$plural and $stringMinutes minutes"
                    vb.nextAlarmIn.text = getString(R.string.upcoming_alarm, alarmString)
                }
            }
            if (hoursUntilAlarmStorage.size != 0) {
                var smallestInt = 999
                for (time in hoursUntilAlarmStorage) {
                    if (time < smallestInt) {
                        smallestInt = time
                    }
                    convertNextAlarmIn(smallestInt)
                }
            } else {
                vb.nextAlarmIn.text = ""
            }

            parentLayout.addView(reminderItemView)
        }
    }

    private fun deleteReminder(reminder: Reminder, secondActivity: secondActivity, remID: Int) {
        reminderArray.remove(reminder)
        update_reminders_count()
        secondActivity.scheduleNotificationCancellation(remID)
        ReminderStorage.saveReminders(this, reminderArray)
        create_reminder_view()
    }


    class timeConverter() {

        fun closestDay(index: Int, reminderDays: MutableList<String>): String {
            var newIndex = index
            var foundDay = false

            while(newIndex <= 6) {
                if (daysMap[newIndex] in reminderDays) {
                    foundDay = true
                    break
                }
                newIndex++
            }

            if (!foundDay) {
                newIndex = 0
                while (newIndex <= 6) {
                    if (daysMap[newIndex] in reminderDays) {
                        foundDay = true
                        break
                    }
                    newIndex++
                }
            }
            return ((daysMap[newIndex].toString()).lowercase()).replaceFirstChar( {it.titlecase()})
        }

        fun days_until(reminderDays: MutableList<String>, removeDay: Boolean): String {

            val daysListCopy = ArrayList(reminderDays)
            val currentDay = LocalDate.now().getDayOfWeek()
            var index = 0

            if (removeDay) {
                daysListCopy.removeAll { it == currentDay.toString() }
            }

                while (daysMap[index] !== currentDay.toString()) {
                    index++
                }

                if (currentDay.toString() in daysListCopy) {
                    Log.d("The days are the same", currentDay.toString())
                } else {
                    return closestDay(index, daysListCopy)
                }
            return "hi"
        }


        fun time_until(reminderTime: Int, currentTime: Int): IntArray {
            val remHours = reminderTime / 100
            val remMinutes = reminderTime % 100
            val currHours = currentTime / 100
            val currMinutes = currentTime % 100

            var hoursDifference = remHours - currHours
            var minutesDifference = remMinutes - currMinutes

            if (minutesDifference < 0) {
                minutesDifference += 60
                hoursDifference--
            }
            if (hoursDifference < 0) {
                hoursDifference += 24
            }
            val timeList = mutableListOf<Int>()
            timeList.add(hoursDifference)
            timeList.add(minutesDifference)
            val timeArray = timeList.toIntArray()
            return timeArray
        }

        fun time_until_formatted(hours: Int, minutes: Int, returnUpcomingAlarms:Boolean, returnAM: Boolean, reminderDays: MutableList<String>): String {


            val currentDay = LocalDate.now().getDayOfWeek()
            if(currentDay.toString() !in reminderDays) {
                val result = days_until(reminderDays, false)
                return result
            }

            if(minutes > hours) {
                val result = days_until(reminderDays, true)
                return result
            }

            val timeValues = time_until(hours, minutes)
            var emptyString = "s"
            var formattedHours = String.format("%02d", timeValues[0])
            val formattedMinutes = String.format("%02d", timeValues[1])
            Log.d("Formatted Hours", formattedHours)
            Log.d("Formatted Minutes", formattedMinutes)

            if (formattedHours.first() == '0') {
                formattedHours = formattedHours[1].toString()
                if (formattedHours.first() == '1')
                    emptyString = ""
            }

            val recentHours = (String.format("%02d", timeValues[0]) + String.format("%02d", timeValues[1])).toInt()
            val timeDiff = formattedHours + formattedMinutes
            hoursUntilAlarmStorage.add(timeDiff.toInt())
            if(returnUpcomingAlarms) {
                return recentHours.toString()
            }

            if (hours != 0) {
                val returnString = "$formattedHours hour$emptyString $formattedMinutes minutes"
                return returnString
            } else {
                val returnString = "$formattedMinutes minutes"
                return returnString
            }
        }

        fun convert_time(time: String, checkAM: Boolean): Int {
            var hours: Int = time.split(":")[0].toInt()
            val minutes: Int = time.split(":")[1].toInt()

            if (!checkAM && hours < 12 && hours > 0) {
                hours += 12
            }

            return hours * 100 + minutes
        }

        fun current_time(): Int {

            val currentTime = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = dateFormat.format(currentTime)
            return convert_time(formattedTime, true)
        }

    }

    override fun onResume() {
        super.onResume()
        hoursUntilAlarmStorage.clear()
        create_reminder_view()
        update_reminders_count()
    }
    override fun onPause() {
        handler.removeCallbacks(refreshRunnable)
        super.onPause()
    }
}