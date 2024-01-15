package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class FileInfo(
    var idFile: String = UUID.randomUUID().toString(),
    var title: String = "",
    var fileUrl: String? = null,
): Parcelable
