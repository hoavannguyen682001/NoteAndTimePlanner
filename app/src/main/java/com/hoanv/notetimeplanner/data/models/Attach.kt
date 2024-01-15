package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Attach(
    var attachId: String = UUID.randomUUID().toString(),
    var listImage: List<ImageInfo> = mutableListOf(),
    var listFile: List<FileInfo> = mutableListOf()
) : Parcelable