package com.hoanv.notetimeplanner.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hoanv.notetimeplanner.data.models.notification.DataTask
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.NOTIFICATION_MESSAGE
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.NOTIFICATION_TITLE
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.TASK_ID
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.notification.NotificationUtil
import com.hoanv.notetimeplanner.utils.notification.isTimeAutomatic
import java.text.SimpleDateFormat
import java.util.Locale

class NoteTimePlannerService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // Get Message details
            val id = remoteMessage.data["taskId"]

            val dataTask = DataTask(
                taskId = id ?: "",
                uniqueId = remoteMessage.data["uniqueId"]!!,
                title = remoteMessage.data["title"]!!,
                content = remoteMessage.data["content"]!!,
                isUpdate = remoteMessage.data["isUpdate"]!!,
                isScheduled = remoteMessage.data["isScheduled"]!!,
                scheduledTime = remoteMessage.data["scheduledTime"]!!
            )

            Log.d("INTENTTTT", "$dataTask")
            // Check that 'Automatic Date and Time' settings are turned ON.
            // If it's not turned on, Return
            if (!isTimeAutomatic(applicationContext)) {
                Log.d(TAG, "`Automatic Date and Time` is not enabled")
                return
            }

            scheduleAlarm(dataTask)
        }
    }

    private fun scheduleAlarm(
        dataTask: DataTask,
    ) {
        val alarmMgr =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (dataTask.isUpdate.toBoolean()) { //User update time schedule
            /* Cancel scheduled setup previous */
            val cancelIntent =
                Intent(
                    applicationContext,
                    NotificationBroadcastReceiver::class.java
                ).let { intent ->
                    intent.putExtra(TASK_ID, dataTask.taskId)
                    intent.putExtra(NOTIFICATION_TITLE, dataTask.title)
                    intent.putExtra(NOTIFICATION_MESSAGE, dataTask.content)
                    PendingIntent.getBroadcast(
                        applicationContext, dataTask.uniqueId.toInt(), intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
            alarmMgr.cancel(cancelIntent)
            Log.d("SCHEDULEEEEEEEEEEEEEEE", "Cancel schedule")

            /* Set schedule time again after cancel if isScheduled is true*/
            if (dataTask.isScheduled.toBoolean()) {
                val alarmIntent =
                    Intent(
                        applicationContext,
                        NotificationBroadcastReceiver::class.java
                    ).let { intent ->
                        intent.putExtra(TASK_ID, dataTask.taskId)
                        intent.putExtra(NOTIFICATION_TITLE, dataTask.title)
                        intent.putExtra(NOTIFICATION_MESSAGE, dataTask.content)
                        PendingIntent.getBroadcast(
                            applicationContext, dataTask.uniqueId.toInt(), intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    }

                // Parse Schedule time
                val scheduledTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                    .parse(dataTask.scheduledTime)

                Log.d("SCHEDULEEEEEEEEEEEEEEE", "$scheduledTime")
                scheduledTime?.let {
                    // With set(), it'll set non repeating one time alarm.
                    alarmMgr.set(
                        AlarmManager.RTC_WAKEUP,
                        it.time,
                        alarmIntent
                    )
                }
            }
        } else {
            if (dataTask.isScheduled.toBoolean()) {
                val alarmIntent =
                    Intent(
                        applicationContext,
                        NotificationBroadcastReceiver::class.java
                    ).let { intent ->
                        intent.putExtra(TASK_ID, dataTask.taskId)
                        intent.putExtra(NOTIFICATION_TITLE, dataTask.title)
                        intent.putExtra(NOTIFICATION_MESSAGE, dataTask.content)
                        PendingIntent.getBroadcast(
                            applicationContext, dataTask.uniqueId.toInt(), intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    }

                // Parse Schedule time
                val scheduledTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                    .parse(dataTask.scheduledTime)

                Log.d("SCHEDULEEEEEEEEEEEEEEE", "$scheduledTime")
                scheduledTime?.let {
                    // With set(), it'll set non repeating one time alarm.
                    alarmMgr.set(
                        AlarmManager.RTC_WAKEUP,
                        it.time,
                        alarmIntent
                    )
                }
            }
        }
    }

    private fun showNotification(dataTask: DataTask) {
        NotificationUtil(applicationContext).showNotification(
            dataTask.taskId,
            dataTask.title,
            dataTask.content
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Pref.deviceToken = token
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}