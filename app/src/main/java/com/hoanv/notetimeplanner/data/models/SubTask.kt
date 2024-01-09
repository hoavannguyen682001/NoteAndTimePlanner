package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class SubTask(
    var taskId: String = UUID.randomUUID().toString(),
    var title: String? = "",
    var isDone: Boolean = false
) : Parcelable