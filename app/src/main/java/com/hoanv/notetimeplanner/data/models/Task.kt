package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import com.aminography.primecalendar.civil.CivilCalendar
import kotlinx.parcelize.Parcelize
import java.util.Calendar

@Parcelize
data class Task(
    var id: String = "",
    var title: String? = "",
    var description: String? = "",
    var category: String? = "",
    var subTask: List<String> = emptyList(),
    var startDay: String? = CivilCalendar.toString(),
    var endDay: String? = CivilCalendar.toString(),
    var timeEnd: String? = Calendar.getInstance().time.toString(),
    var listAttach: List<String> = emptyList(),
    var statusTask: Boolean = false
) : Parcelable