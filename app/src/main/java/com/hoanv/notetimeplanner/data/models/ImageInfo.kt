package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ImageInfo(
    var idImage: String = UUID.randomUUID().toString(),
    var title: String = "",
    var imageUrl: String? = null,
) : Parcelable