package com.example.reminderapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.reminderapp.databinding.ActivitySettingsBinding

private lateinit var vb: ActivitySettingsBinding
class activity_settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.backButton2.setOnClickListener {
            finish()
        }
    }
}