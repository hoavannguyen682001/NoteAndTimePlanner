package com.hoanv.notetimeplanner.utils.extension

import android.view.View

class OnSingleClickListener : View.OnClickListener {
    private val onClickListener: View.OnClickListener
    private var previousClickTimeMillis = 0L

    constructor(listener: View.OnClickListener) {
        onClickListener = listener
    }

    constructor(listener: (View) -> Unit) {
        onClickListener = View.OnClickListener { listener.invoke(it) }
    }

    override fun onClick(v: View) {
        val currentTimeMillis = System.currentTimeMillis()

        if (currentTimeMillis >= previousClickTimeMillis + DELAY_MILLIS) {
            previousClickTimeMillis = currentTimeMillis
            onClickListener.onClick(v)
        }
    }

    companion object {
        private const val DELAY_MILLIS = 2000L
    }

}