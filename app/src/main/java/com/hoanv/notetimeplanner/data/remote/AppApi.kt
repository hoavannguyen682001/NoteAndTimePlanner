package com.hoanv.notetimeplanner.data.remote

import com.hoanv.notetimeplanner.data.models.group.GroupNotification
import com.hoanv.notetimeplanner.data.models.group.ResponseKey
import com.hoanv.notetimeplanner.data.models.notification.NotificationData
import com.hoanv.notetimeplanner.data.models.notification.ResponseNoti
import retrofit2.http.Body
import retrofit2.http.POST

interface AppApi {
    @POST("v1/projects/timeplaner-f8621/messages:send")
    suspend fun sendNotification(@Body body: NotificationData): ResponseNoti

    @POST("fcm/notification")
    suspend fun createGroupNotification(@Body body: GroupNotification): ResponseKey
}