package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class NotificationInfo(
    var notiId: String = UUID.randomUUID().toString(),
    var taskId: String = "",
    var title: String = "",
    var content: String = "",
    var scheduleTime: String = "",
) : Parcelable {
    fun hashMap(): Map<String, Any?> {
        return mapOf(
            "id" to notiId,
            "title" to title,
            "content" to content,
            "scheduleTime" to scheduleTime
        )
    }
}
