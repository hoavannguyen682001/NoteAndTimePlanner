package com.hoanv.notetimeplanner.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.NOTIFICATION_MESSAGE
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.NOTIFICATION_TITLE
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.TASK_ID

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val id = it.getStringExtra(TASK_ID)
            val title = it.getStringExtra(NOTIFICATION_TITLE)
            val message = it.getStringExtra(NOTIFICATION_MESSAGE)

            // Create Notification Data
            val notificationData = Data.Builder()
                .putString(TASK_ID, id)
                .putString(NOTIFICATION_TITLE, title)
                .putString(NOTIFICATION_MESSAGE, message)
                .build()

            // Init Worker
            val work = OneTimeWorkRequest.Builder(ScheduledWorker::class.java)
                .setInputData(notificationData)
                .build()

            // Start Worker
            if (context != null) {
                WorkManager.getInstance(context).beginWith(work).enqueue()
            }

            Log.d(javaClass.name, "WorkManager is Enqueued.")
        }
    }
}