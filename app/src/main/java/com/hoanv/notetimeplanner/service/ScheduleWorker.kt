package com.hoanv.notetimeplanner.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hoanv.notetimeplanner.utils.notification.NotificationUtil

class ScheduledWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {

        Log.d(TAG, "Work START")

        // Get Notification Data
        val taskId = inputData.getString(TASK_ID)
        val title = inputData.getString(NOTIFICATION_TITLE)
        val message = inputData.getString(NOTIFICATION_MESSAGE)

        // Show Notification
        NotificationUtil(applicationContext).showNotification(taskId!!, title!!, message!!)

        // TODO Do your other Background Processing

        Log.d(TAG, "Work DONE")
        // Return result

        return Result.success()
    }

    companion object {
        private const val TAG = "ScheduledWorker"
        const val TASK_ID = "task_id"
        const val NOTIFICATION_TITLE = "notification_title"
        const val NOTIFICATION_MESSAGE = "notification_message"
    }
}