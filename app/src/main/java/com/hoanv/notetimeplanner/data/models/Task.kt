package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import com.aminography.primecalendar.civil.CivilCalendar
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Parcelize
data class Task(
    var id: String = UUID.randomUUID().toString(),
    var title: String? = "",
    var description: String? = "",
    var category: Category = Category(title = "Không có thể loại"),
    var subTask: List<SubTask> = mutableListOf(),
    var startDay: String? = CivilCalendar.toString(),
    var endDay: String? = CivilCalendar.toString(),
    var timeEnd: String? = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    var listAttach: List<Attach> = mutableListOf(),
    var taskState: Boolean = false
) : Parcelable {

    fun hashMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "category" to category,
            "subTask" to subTask,
            "startDay" to startDay,
            "endDay" to endDay,
            "timeEnd" to timeEnd,
            "listAttach" to listAttach,
            "taskState" to taskState
        )
    }

    fun ownCopy(): Task {
        return this.copy().apply {
            taskState = this@Task.taskState
        }
    }
}