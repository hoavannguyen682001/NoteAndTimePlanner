package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar
import java.util.UUID

@Parcelize
data class Category(
    var id: String = UUID.randomUUID().toString(),
    var title: String? = "",
    var createdTime: String = Calendar.getInstance().timeInMillis.toString()
) : Parcelable {
    fun hashMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "createdTime" to createdTime
        )
    }
}
