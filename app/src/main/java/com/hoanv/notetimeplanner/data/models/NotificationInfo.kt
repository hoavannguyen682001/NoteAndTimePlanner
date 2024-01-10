package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class NotificationInfo(
    var notiId: String = UUID.randomUUID().toString(),
    var taskId: String = "",
    var uniqueId: Long = 0,
    var title: String = "",
    var content: String = "",
    var notificationKey: String = "",
    var member: MutableList<String> = mutableListOf(),
    var dayNotification: String = "",
    var timeNotification: String = "",
) : Parcelable {
    fun hashMap(): Map<String, Any?> {
        return mapOf(
            "id" to notiId,
            "title" to title,
            "content" to content,
            "dayNotification" to dayNotification,
            "timeNotification" to timeNotification,
            "notificationKey" to notificationKey,
            "member" to member
        )
    }
}
