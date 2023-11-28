package fxc.dev.common.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment

/**
 *
 * Created by tamle on 09/06/2023
 *
 */

fun Context.resourceColor(@ColorRes res: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getColor(res, theme)
    } else {
        resources.getColor(res)
    }
}

fun Fragment.resourceColor(@ColorRes res: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getColor(res, requireActivity().theme)
    } else {
        resources.getColor(res)
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
fun Fragment.resourceDrawable(@DrawableRes res: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getDrawable(res, requireActivity().theme)
    } else {
        resources.getDrawable(res)
    }
}

fun View.setBackground(startColor: Int, endColor: Int, cornerRadius: Float) {
    val gradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(startColor, endColor)
    );
    gradientDrawable.cornerRadius = cornerRadius;
    background = gradientDrawable
}