package com.hoanv.notetimeplanner.ui.main.tasks.create

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TimePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.RangeDaysPickCallback
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.databinding.ActivityAddTaskBinding
import com.hoanv.notetimeplanner.databinding.DialogLabelBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.tasks.create.dialog.TimePickerFragment
import com.hoanv.notetimeplanner.utils.extension.safeClickListener
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTaskActivity : BaseActivity<ActivityAddTaskBinding, AddTaskVM>(),
    TimePickerFragment.TimePickerListener {
    override val viewModel: AddTaskVM by viewModels()

    private val timePickerFrag = TimePickerFragment()

    override fun init(savedInstanceState: Bundle?) {
        timePickerFrag.setDataTimePicker(this@AddTaskActivity)
        initListener()
    }

    private fun initListener() {
        binding.run {
            ivClose.setOnSingleClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            tvEstimate.safeClickListener {
                dateRangePicker().show(supportFragmentManager, "DatePicker")
            }
            tvTimer.safeClickListener {
                timePickerFrag.show(supportFragmentManager, "TimerPicker")
            }
            tvLabel.safeClickListener {
                dialogLabel()
            }
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityAddTaskBinding =
        ActivityAddTaskBinding.inflate(inflater)

    override fun timePickerListener(
        view: TimePicker,
        hourOfDay: Int,
        minute: Int
    ) {
        binding.tvTimer.text = getString(R.string.time_picker, hourOfDay, minute)
    }

    private fun dateRangePicker(): PrimeDatePicker {
        val callback = RangeDaysPickCallback { str, end ->
            binding.tvEstimate.text =
                getString(
                    R.string.date_range_selected,
                    str.date,
                    str.month,
                    str.year,
                    end.date,
                    end.month,
                    end.year
                )
        }
        return PrimeDatePicker.bottomSheetWith(CivilCalendar())
            .pickRangeDays(callback)
            .initiallyPickedStartDay(CivilCalendar())
            .build()
    }

    private fun dialogLabel() {
        val dialogBinding = DialogLabelBinding.inflate(LayoutInflater.from(this))
        val alertDialog =
            AlertDialog.Builder(this, R.style.AppCompat_AlertDialog)
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()
        binding.run {
            dialogBinding.run {
                tvTaskNew.setOnSingleClickListener {
                    tvLabel.text = tvTaskNew.text
                    alertDialog.dismiss()
                }
                tvTaskUpdate.setOnSingleClickListener {
                    tvLabel.text = tvTaskUpdate.text
                    alertDialog.dismiss()
                }
                tvResearch.setOnSingleClickListener {
                    tvLabel.text = tvResearch.text
                    alertDialog.dismiss()
                }
                tvTaskUrgent.setOnSingleClickListener {
                    tvLabel.text = tvTaskUrgent.text
                    alertDialog.dismiss()
                }
            }
        }
        alertDialog.show()
    }
}