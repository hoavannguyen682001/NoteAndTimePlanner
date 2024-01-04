package com.hoanv.notetimeplanner.data.models.notification

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class MessageTask(
    @SerializedName("data")
    var data: DataTask,
    @SerializedName("token")
    var token: String,
)