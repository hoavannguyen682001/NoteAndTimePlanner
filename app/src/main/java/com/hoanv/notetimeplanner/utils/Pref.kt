package com.hoanv.notetimeplanner.utils

import com.chibatching.kotpref.KotprefModel

object Pref : KotprefModel() {
    var isSaveLogin by booleanPref(false)
    var deviceToken by stringPref(default = "")
    var accessToken by stringPref(default = "")
}