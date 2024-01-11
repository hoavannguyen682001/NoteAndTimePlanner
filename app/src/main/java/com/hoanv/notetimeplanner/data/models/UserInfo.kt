package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Parcelize
data class UserInfo(
    var uid: String = UUID.randomUUID().toString(),
    var userName: String = "",
    var gender: String = "Nam",
    var birthDay: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
    var userEmail: String = "",
    var userPassword: String = "",
    var photoUrl: String? = null,
    var userToken: String = ""
) : Parcelable {
    fun hashMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "userName" to userName,
            "gender" to gender,
            "birthDay" to birthDay,
            "userEmail" to userEmail,
            "userPassword" to userPassword,
            "photoUrl" to photoUrl,
            "userToken" to userToken
        )
    }
}