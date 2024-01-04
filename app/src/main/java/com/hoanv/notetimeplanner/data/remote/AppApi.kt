package com.hoanv.notetimeplanner.data.remote

import com.hoanv.notetimeplanner.data.models.notification.NotificationData
import com.hoanv.notetimeplanner.data.models.notification.ResponseNoti
import retrofit2.http.Body
import retrofit2.http.POST

interface AppApi {
    @POST("projects/timeplaner-f8621/messages:send")
    suspend fun sendNotification(@Body body: NotificationData): ResponseNoti

}