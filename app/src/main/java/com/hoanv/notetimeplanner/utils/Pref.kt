package com.hoanv.notetimeplanner.utils

import com.chibatching.kotpref.KotprefModel

object Pref: KotprefModel() {
    var isSaveLogin by booleanPref(false)
}