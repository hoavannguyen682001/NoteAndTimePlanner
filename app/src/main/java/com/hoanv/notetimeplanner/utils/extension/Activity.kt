package com.hoanv.notetimeplanner.utils.extension

import android.app.Activity
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import fxc.dev.common.extension.resourceColor


fun Activity.changeStatusStyle(@ColorRes bgColor: Int, isLightForegroundColor: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = resourceColor(bgColor)
    }
    if (!isLightForegroundColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun Activity.hideSoftKeyboard() {
    val inputMethodManager: InputMethodManager = getSystemService(
        Activity.INPUT_METHOD_SERVICE
    ) as InputMethodManager
    if (inputMethodManager.isAcceptingText) {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocus!!.windowToken,
            0
        )
    }
}

fun AppCompatActivity.addOnBackPressedDispatcher(onBackTapped: () -> Unit = { finish() }) {

    onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackTapped.invoke()

                // Calling onBackPressed() on an Activity with its state saved can cause an
                // error on devices on API levels before 26. We catch that specific error and
                // throw all others.
                try {
                    onBackPressed()
                } catch (e: IllegalStateException) {
                    if (!TextUtils.equals(
                            e.message,
                            "Can not perform this action after onSaveInstanceState"
                        )
                    ) {
                        throw e
                    }
                }
            }
        }
    )
}