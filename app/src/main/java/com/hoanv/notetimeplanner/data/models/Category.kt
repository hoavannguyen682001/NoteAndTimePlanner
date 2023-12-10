package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar
import java.util.UUID

@Parcelize
data class Category(
    var id: String = UUID.randomUUID().toString(),
    var title: String? = "",
    var createdAt: String = Calendar.getInstance().timeInMillis.toString(),
    var listTodo: List<Todo> = mutableListOf(),
    var isSelected: Boolean = false
) : Parcelable {
    fun hashMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "createdAt" to createdAt,
            "listTask" to listTodo
        )
    }

    fun ownCopy(): Category {
        return this.copy().apply {
            isSelected = this@Category.isSelected
        }
    }
}
