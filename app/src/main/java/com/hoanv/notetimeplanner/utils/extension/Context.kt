package com.hoanv.notetimeplanner.utils.extension

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openWebPage(url: String, noActivityFound: (() -> Unit)? = null) {
    var newUrl = url
    if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
        newUrl = "http://$newUrl"
    }
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(newUrl)))
    } catch (ex: ActivityNotFoundException) {
        noActivityFound?.invoke()
    }
}