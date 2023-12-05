package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import androidx.room.Ignore
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Calendar
import java.util.UUID

@Parcelize
data class Category(
    var id: String = UUID.randomUUID().toString(),
    var title: String? = "",
    var createdTime: String = Calendar.getInstance().timeInMillis.toString(),
    var listTask: Int = 0,
    var isSelected: Boolean = false
) : Parcelable {
    fun hashMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "createdTime" to createdTime
        )
    }

    fun ownCopy(): Category {
        return this.copy().apply {
            isSelected = this@Category.isSelected
        }
    }
}
