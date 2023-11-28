package com.hoanv.notetimeplanner.utils.extension

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.roundToInt

fun Float.dpToPx(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics
    )
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()