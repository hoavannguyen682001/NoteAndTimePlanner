package com.hoanv.notetimeplanner.utils.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.TASK_ID
import com.hoanv.notetimeplanner.ui.main.tasks.create.AddTaskActivity
import com.hoanv.notetimeplanner.utils.AppConstant
import kotlin.random.Random

class NotificationUtil(private val context: Context) {

    fun showNotification(taskId: String, title: String, message: String) {
        val intent = Intent(context, AddTaskActivity::class.java)
        intent.putExtra(TASK_ID, taskId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, AppConstant.NOTIFICATION_ID)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }
}