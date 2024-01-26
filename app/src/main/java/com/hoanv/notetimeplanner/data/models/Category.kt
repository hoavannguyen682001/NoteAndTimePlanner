package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar
import java.util.UUID

@Parcelize
data class Category(
    var id: String = UUID.randomUUID().toString(),
    var title: String? = "",
    var listTask: Int = 0,
    var icon: Icon = Icon(),
    var isDefault: Boolean = false,
    var createdAt: String = Calendar.getInstance().timeInMillis.toString(),
    var isSelected: Boolean = false
) : Parcelable {
    fun hashMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "icon" to icon,
            "default" to isDefault,
            "listTask" to listTask,
            "createdAt" to createdAt
        )
    }

    fun ownCopy(): Category {
        return this.copy().apply {
            isSelected = this@Category.isSelected
        }
    }
}
