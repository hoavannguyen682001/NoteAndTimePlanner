package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Attach(
    var taskId: String = "",
    var image: String? = "",
    var file: String? = ""
) : Parcelable