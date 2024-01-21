package com.hoanv.notetimeplanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.hoanv.notetimeplanner.utils.AppConstant
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class Application : Application() {
    private lateinit var manager: NotificationManager
    override fun onCreate() {
        super.onCreate()
        val vietnameseLocale = Locale("vi", "VN")
        val resources: Resources = resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(vietnameseLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        createChannelNotification()
    }

    private fun createChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstant.NOTIFICATION_ID,
                "Push Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        } else {
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
    }
}