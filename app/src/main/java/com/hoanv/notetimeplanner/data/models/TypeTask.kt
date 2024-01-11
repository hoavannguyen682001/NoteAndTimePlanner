package com.hoanv.notetimeplanner.data.models

import com.google.gson.annotations.SerializedName

enum class TypeTask {
    @SerializedName("personal")
    PERSONAL,

    @SerializedName("group")
    GROUP
}