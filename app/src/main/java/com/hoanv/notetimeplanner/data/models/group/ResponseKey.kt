package com.hoanv.notetimeplanner.data.models.group

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseKey(
    @SerializedName("notification_key")
    var notificationKey: String
)
