package com.hoanv.notetimeplanner.utils.extension

import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

fun String.formatKM(): String {
    val number = this.toLongOrNull() ?: return this
    if (abs(number / 1000000) > 1) return (number / 1000000).toString() + "m"
    else if (abs(number / 1000) > 1) return (number / 1000).toString() + "k"
    return this
}

//fun String.toJSON(): Any {
//    return try {
//        val parser = JsonParser.parseString(this)
//        if (parser.isJsonObject) {
//            JSONObject(this)
//        } else {
//            JSONArray(this)
//        }
//    } catch (ignore: Throwable) {
//        this
//    }
//}