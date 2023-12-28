package com.hoanv.notetimeplanner.utils

object NetworkConstant {
    const val API_SERVER = "https://fcm.googleapis.com/v1/projects/timeplaner-f8621/messages:send"
    const val TIME_OUT = 30_000L
    const val CACHE_SIZE = (5 * 1024 * 1024).toLong()
}
