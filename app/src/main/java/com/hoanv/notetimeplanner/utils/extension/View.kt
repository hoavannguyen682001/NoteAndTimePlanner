package com.hoanv.notetimeplanner.utils.extension

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DimenRes
import androidx.cardview.widget.CardView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import kotlin.math.abs

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.setSize(@DimenRes height: Int? = null, @DimenRes width: Int? = null) {
    val params: ViewGroup.LayoutParams = this.layoutParams
    if (height != null) {
        params.height = height
    }
    if (width != null) {
        params.width = width
    }
    this.layoutParams = params
}

/**
 * This method will be used when we want to hide keyboard of view
 */
@SuppressLint("ClickableViewAccessibility")
fun View.touchHideKeyboard(isClearFocus: Boolean = true) {
    // Set up touch listener for non-text box views to hide keyboard.
    if (this !is EditText || !this.isFocusable()) {
        this.setOnTouchListener { _, _ ->
            this.hideKeyboard()
            if (isClearFocus) {
                this.clearFocus()
            }
            false
        }
    }
    if (this is ViewGroup) {
        for (i in 0 until this.childCount) {
            this.getChildAt(i)
            val innerView = this.getChildAt(i)
            innerView.touchHideKeyboard()
        }
    }
}

fun View.hideKeyboard() =
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
        hideSoftInputFromWindow(this@hideKeyboard.windowToken, 0)
    }

fun ViewGroup.slideFromLeft() {
    val enterTransition = Slide(Gravity.START)
    TransitionManager.beginDelayedTransition(this, enterTransition)
}

fun ViewGroup.slideFromRight() {
    val enterTransition = Slide(Gravity.END)
    TransitionManager.beginDelayedTransition(this, enterTransition)
}

fun View.rotation(degree: Float) {
    val f = this.rotation
    val animator = ObjectAnimator.ofFloat(this, "rotation", f, degree)
    animator.start()
}

/**
 * This function to click view for safe
 */
fun View.safeClickListener(safeClickListener: (view: View) -> Unit) {
    this.setOnClickListener {
        if (!SingleClick.isBlockingClick()) {
            safeClickListener(it)
        }
    }
}

object SingleClick {
    private const val MIN_CLICK_INTERVAL = 200
    private var sLastClickTime: Long = 0

    fun isBlockingClick(): Boolean = isBlockingClick(MIN_CLICK_INTERVAL.toLong())

    @Suppress("SameParameterValue")
    private fun isBlockingClick(minClickInterval: Long): Boolean {
        val isBlocking: Boolean
        val currentTime = System.currentTimeMillis()
        isBlocking = abs(currentTime - sLastClickTime) < minClickInterval
        if (!isBlocking) {
            sLastClickTime = currentTime
        }
        return isBlocking
    }
}

const val ANIMATION_FAST_MILLIS = 50L

fun CardView.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
    performClick()
    isPressed = true
    invalidate()
    postDelayed({
        invalidate()
        isPressed = false
    }, delay)
}



