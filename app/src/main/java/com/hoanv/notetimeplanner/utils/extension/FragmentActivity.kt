package com.hoanv.notetimeplanner.utils.extension

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.selectSingleImage(requestCode: Int) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.apply {
        type = "image/*"
    }
    this.startActivityForResult(Intent.createChooser(intent, "Select picture"), requestCode)
}

fun FragmentActivity.gotoSettingPermission() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}