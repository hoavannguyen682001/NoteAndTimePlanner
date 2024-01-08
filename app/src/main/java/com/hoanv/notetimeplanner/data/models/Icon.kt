package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Icon(
    var iconId: String = UUID.randomUUID().toString(),
    var iconUrl: String = "",
    var iconColor: String = "",
    var isSelected: Boolean = false
) : Parcelable {
    fun ownCopy(): Icon {
        return this.copy().apply {
            isSelected = this@Icon.isSelected
        }
    }
}