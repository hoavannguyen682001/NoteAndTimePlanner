package com.hoanv.notetimeplanner.utils

import com.chibatching.kotpref.KotprefModel

object Pref : KotprefModel() {
    var isSaveLogin by booleanPref(false)
    var userId by stringPref(default = "")
    var userEmail by stringPref(default = "")
    var deviceToken by stringPref(default = "")
    var accessToken by stringPref(default = "")
    var mTaskUserId by stringPref(default = "")
    var isLoading by booleanPref(default = false)

}