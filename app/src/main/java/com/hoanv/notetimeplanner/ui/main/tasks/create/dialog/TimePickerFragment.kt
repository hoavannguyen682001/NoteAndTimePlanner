package com.hoanv.notetimeplanner.ui.main.tasks.create.dialog

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class TimePickerFragment : DialogFragment() {
    private val c = Calendar.getInstance()
    private val hour = c.get(Calendar.HOUR_OF_DAY)
    private val minute = c.get(Calendar.MINUTE)

    private var timePicker: TimePickerListener? = null

    private val onTimePicker = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        timePicker?.timePickerListener(view, hourOfDay, minute)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(
            activity,
            onTimePicker,
            hour,
            minute,
            true
        )
    }

    fun setDataTimePicker(listener: TimePickerListener) {
        timePicker = listener
    }

    interface TimePickerListener {
        fun timePickerListener(
            view: TimePicker,
            hourOfDay: Int,
            minute: Int
        )
    }
}