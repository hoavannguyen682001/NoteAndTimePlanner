package com.hoanv.notetimeplanner.data.models.notification

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationData(
    @SerializedName("message")
    val message: MessageTask
)
