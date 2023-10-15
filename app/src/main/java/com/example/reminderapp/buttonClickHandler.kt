package com.example.reminderapp

import android.content.Context
import android.content.Intent
import com.example.reminderapp.databinding.ActivityMainBinding


fun handleButtonClicks(vb: ActivityMainBinding, context: Context) {
    vb.addReminderButton.setOnClickListener {
        val intent = Intent(context, secondActivity::class.java)
        context.startActivity(intent)
    }

    vb.settingsButton.setOnClickListener {
        val intent = Intent(context, activity_settings::class.java)
        context.startActivity(intent)
    }

    vb.helpButton.setOnClickListener {
        val intent = Intent(context, activity_help::class.java)
        context.startActivity(intent)
    }
}