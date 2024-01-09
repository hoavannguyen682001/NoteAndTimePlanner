package com.hoanv.notetimeplanner.data.models


data class GroupNotiInfo(
    var id: String = "",
    var unique: Int = 0,
    var taskId: String = "",
    var notificationKey: String = "",
    var member: MutableList<String> = mutableListOf(),
)
