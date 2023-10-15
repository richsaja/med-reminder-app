package com.example.reminderapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.reminderapp.databinding.ActivityHelpBinding

private lateinit var vb: ActivityHelpBinding
class activity_help : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.backButton3.setOnClickListener {
            finish()
        }
    }
}