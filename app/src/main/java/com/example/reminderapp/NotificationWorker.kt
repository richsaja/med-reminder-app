package com.example.reminderapp

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val notificationId = inputData.getInt("NOTIFICATION_ID", 0)
        val channelId = inputData.getString("CHANNEL_ID") ?: ""

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val cancelNotificationRequested = inputData.getBoolean("CANCEL_NOTIFICATION", false)
        if (cancelNotificationRequested) {
            cancelNotification(notificationId)
            return Result.success()
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Example Title")
            .setContentText("Example Description")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(notificationId, builder.build())

        return Result.success()
    }

    private fun cancelNotification(notificationId: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
        Log.d("NotificationWorker", "Cancelled notification with ID: $notificationId")
    }
}