package com.hoanv.notetimeplanner.utils.extension

import android.view.View
import com.hoanv.notetimeplanner.utils.extension.OnSingleClickListener

fun View.setOnSingleClickListener(listener: View.OnClickListener) {
    setOnClickListener(OnSingleClickListener(listener))
}

fun View.setOnSingleClickListener(listener: (View) -> Unit) {
    setOnClickListener(OnSingleClickListener(listener))
}