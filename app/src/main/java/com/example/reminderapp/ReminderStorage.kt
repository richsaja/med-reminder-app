package com.example.reminderapp

import android.content.Context
import com.google.gson.Gson

object ReminderStorage {

    private const val PREF_NAME = "ReminderPrefs"
    private const val KEY_REMINDERS = "reminders"
    private const val KEY_COUNTER = "reminder_counter"


    private val gson = Gson()

    fun getReminderCounter(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(KEY_COUNTER, 0)
    }

    fun incrementReminderCounter(context: Context) {
        val currentCounter = getReminderCounter(context)
        val newCounter = currentCounter + 1
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(KEY_COUNTER, newCounter).apply()
    }

    fun saveReminders(context: Context, reminders: List<Reminder>) {
        val remindersJson = gson.toJson(reminders)
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_REMINDERS, remindersJson).apply()
    }
    fun serializeReminders(reminders: List<Reminder>): String {
        return gson.toJson(reminders)
    }
    fun clearAll(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(KEY_REMINDERS).apply()
    }

    fun clearReminder(context: Context, reminderId: Int) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val remindersJson = sharedPreferences.getString(KEY_REMINDERS, null)
        val existingReminders = if (remindersJson != null) {
            gson.fromJson(remindersJson, Array<Reminder>::class.java).toMutableList()
        } else {
            mutableListOf()
        }

        val updatedReminders = existingReminders.filterNot { it.ID == reminderId }

        val updatedRemindersJson = gson.toJson(updatedReminders)
        sharedPreferences.edit().putString(KEY_REMINDERS, updatedRemindersJson).apply()
    }

    fun getReminders(context: Context): List<Reminder> {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val remindersJson = sharedPreferences.getString(KEY_REMINDERS, null)
        return if (remindersJson != null) {
            gson.fromJson(remindersJson, Array<Reminder>::class.java).toList()
        } else {
            emptyList()
        }
    }
    fun updateReminder(context: Context, updatedReminder: Reminder) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val remindersJson = sharedPreferences.getString(KEY_REMINDERS, null)
        val existingReminders = if (remindersJson != null) {
            gson.fromJson(remindersJson, Array<Reminder>::class.java).toMutableList()
        } else {
            mutableListOf()
        }

        val index = existingReminders.indexOfFirst { it.ID == updatedReminder.ID }
        if (index != -1) {
            existingReminders[index] = updatedReminder
            val updatedRemindersJson = gson.toJson(existingReminders)
            sharedPreferences.edit().putString(KEY_REMINDERS, updatedRemindersJson).apply()
        }
    }
}