package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubTask(
    var taskId: String = "",
    var title: String? = "",
    var isDone: Boolean = false
) : Parcelable