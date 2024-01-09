package com.hoanv.notetimeplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class UserInfo(
    var uid: String = UUID.randomUUID().toString(),
    var userName: String = "",
    var userEmail: String = "",
    var userPassword: String = "",
    var photoUrl: String? = "",
    var userToken: String = ""
) : Parcelable {
    fun hashMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "userName" to userName,
            "userEmail" to userEmail,
            "userPassword" to userPassword,
            "photoUrl" to photoUrl,
            "userToken" to userName
        )
    }
}