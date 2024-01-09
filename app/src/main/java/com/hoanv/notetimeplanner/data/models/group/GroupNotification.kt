package com.hoanv.notetimeplanner.data.models.group

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GroupNotification(
    @SerializedName("operation")
    var operation: String,

    @SerializedName("notification_key_name")
    var notificationKeyName: String,

    @SerializedName("notification_key")
    var notificationKey: String?,

    @SerializedName("registration_ids")
    var registrationIds: List<String>
)