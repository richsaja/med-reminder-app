package com.example.reminderapp

import java.io.Serializable

data class Reminder(var title: String, var dosage: String, var time: String, var isAM: Boolean, val ID: Int, var checkedDays: MutableList<String>, var reminderID: Int):
    Serializable