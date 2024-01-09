package com.hoanv.notetimeplanner.data.models.notification

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class DataTask(
    @SerializedName("taskId")
    var taskId: String,
    @SerializedName("uniqueId")
    var uniqueId: String,
    @SerializedName("title")
    var title: String,
    @SerializedName("content")
    var content: String,
    @SerializedName("isUpdate")
    var isUpdate: String,
    @SerializedName("isScheduled")
    var isScheduled: String,
    @SerializedName("scheduledTime")
    var scheduledTime: String
)
