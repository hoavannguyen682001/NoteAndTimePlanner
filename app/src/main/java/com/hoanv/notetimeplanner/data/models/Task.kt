package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import com.aminography.primecalendar.civil.CivilCalendar
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Parcelize
data class Task(
    var id: String = UUID.randomUUID().toString(),
    var userId: String = "",
    var title: String? = "",
    var description: String? = "",
    var category: Category = Category(title = "Không có thể loại"),
    var member: MutableList<UserInfo> = mutableListOf(),
    var startDay: String? = CivilCalendar.toString(),
    var endDay: String? = CivilCalendar.toString(),
    var uniqueId: Int = 0,
    var timeEnd: String? = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    var scheduledTime: String? = null,
    var subTask: List<SubTask> = mutableListOf(),
    var listAttach: List<Attach> = mutableListOf(),
    var taskState: Boolean = false,
    var typeTask: TypeTask = TypeTask.PERSONAL,
    var createdAt: String = Calendar.getInstance().timeInMillis.toString()
) : Parcelable {

    fun hashMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "description" to description,
            "category" to category,
            "subTask" to subTask,
            "uniqueId" to uniqueId,
            "startDay" to startDay,
            "endDay" to endDay,
            "timeEnd" to timeEnd,
            "scheduledTime" to scheduledTime,
            "listAttach" to listAttach,
            "taskState" to taskState,
            "typeTask" to typeTask,
            "createdAt" to createdAt
        )
    }

    fun ownCopy(): Task {
        return this.copy().apply {
            taskState = this@Task.taskState
        }
    }
}