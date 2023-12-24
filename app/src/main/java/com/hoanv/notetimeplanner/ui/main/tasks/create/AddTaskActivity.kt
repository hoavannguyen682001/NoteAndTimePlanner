package com.hoanv.notetimeplanner.ui.main.tasks.create

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TimePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.RangeDaysPickCallback
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.databinding.ActivityAddTaskBinding
import com.hoanv.notetimeplanner.databinding.DialogCategoryBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.tasks.create.adapter.CategoryAdapter
import com.hoanv.notetimeplanner.ui.main.tasks.create.dialog.TimePickerFragment
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import com.hoanv.notetimeplanner.utils.extension.visible
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AddTaskActivity : BaseActivity<ActivityAddTaskBinding, AddTaskVM>(),
    TimePickerFragment.TimePickerListener {
    override val viewModel: AddTaskVM by viewModels()

    private val timePickerFrag = TimePickerFragment()

    private val categoryAdapter by lazy {
        CategoryAdapter(this, ::onCategoryClick)
    }

    private lateinit var dialogBinding: DialogCategoryBinding
    private lateinit var alertDialog: AlertDialog

    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val date = Date()
    private val current = formatter.format(date)
    private var idTodo: String? = null

    private lateinit var mCategory: Category

    override fun init(savedInstanceState: Bundle?) {
        timePickerFrag.setDataTimePicker(this@AddTaskActivity)
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.run {
            val task = intent.getParcelableExtra<Task>("TODO")
            task?.let {
                idTodo = it.id
                loadDataView(it)
            }

            dialogBinding =
                DialogCategoryBinding.inflate(android.view.LayoutInflater.from(this@AddTaskActivity))
            dialogBinding.run {
                rvListCategory.run {
                    adapter = categoryAdapter
                    layoutManager = LinearLayoutManager(
                        this@AddTaskActivity, LinearLayoutManager.HORIZONTAL, false
                    )
                }
            }

            alertDialog = AlertDialog.Builder(this@AddTaskActivity, R.style.AppCompat_AlertDialog)
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()

            tvStartDay.text = current

            rvListCategory.run {
                adapter = categoryAdapter
                layoutManager = LinearLayoutManager(
                    this@AddTaskActivity, LinearLayoutManager.HORIZONTAL, false
                )
            }
        }
    }

    private fun initListener() {
        binding.run {
            ivClose.setOnSingleClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

//            tvEstimate.setOnSingleClickListener {
//                dateRangePicker().show(supportFragmentManager, "DatePicker")
//            }
//
//            tvTimer.setOnSingleClickListener {
//                timePickerFrag.show(supportFragmentManager, "TimerPicker")
//            }
//
//            tvCategory.setOnSingleClickListener {
//                alertDialog.show()
//            }

            ivSubmit.setOnSingleClickListener {
                addTask()
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                listCategory.observe(this@AddTaskActivity) {
                    if (it.isNotEmpty()) {
                        categoryAdapter.submitList(it)
                    }
                }

                addTaskTriggerS.observe(this@AddTaskActivity) { state ->
//                    when (state) {
//                        ResponseState.Start -> {
//                            pbLoading.visible()
//                        }
//
//                        is ResponseState.Success -> {
//                            pbLoading.gone()
//                            toastSuccess(state.data)
//                            finish()
//                        }
//
//                        is ResponseState.Failure -> {
//                            toastError(state.throwable?.message)
//                        }
//                    }
                }

                updateTaskTriggerS.observe(this@AddTaskActivity) { state ->
//                    when (state) {
//                        ResponseState.Start -> {
//                            pbLoading.visible()
//                        }
//
//                        is ResponseState.Success -> {
//                            pbLoading.gone()
//                            toastSuccess(state.data)
//                            finish()
//                        }

//                        is ResponseState.Failure -> {
//                            toastError(state.throwable?.message)
//                        }
//                    }
                }
            }
        }
    }

    private fun loadDataView(task: Task) {
        binding.run {
            edtTitle.setText(task.title)
            edtDescription.setText(task.description)
            tvStartDay.text = task.startDay
            tvEndDay.text = task.endDay
            tvTimeEnd.text = task.timeEnd
//            tvTitLeCategory.text = task.category.title
        }
    }

    private fun addTask() {
        binding.run {
            val task = Task(
                title = edtTitle.text.toString(),
                description = edtDescription.text.toString(),
                category = mCategory,
                timeEnd = tvTimeEnd.text.toString(),
                startDay = tvStartDay.text.toString(),
                endDay = tvEndDay.text.toString(),
                taskState = false
            )
            if (!idTodo.isNullOrEmpty()) {
                task.id = idTodo!!
                viewModel.updateTask(task)
            } else {
                viewModel.addNewTask(task)
                mCategory.listTask++
                viewModel.updateCategory(mCategory, "listTask")
            }
        }
    }

    private fun onCategoryClick(category: Category) {
        mCategory = category
        binding.run {
//            tvTitLeCategory.text = category.title ?: "Không có thể loại"
        }
        alertDialog.dismiss()
    }

    private fun dateRangePicker(): PrimeDatePicker {
        val callback = RangeDaysPickCallback { str, end ->
            binding.run {
                tvStartDay.text = getString(
                    R.string.date_range_selected,
                    str.date,
                    str.month + 1,
                    str.year
                )

                tvEndDay.text = getString(
                    R.string.date_range_selected,
                    end.date,
                    end.month + 1,
                    end.year
                )
            }
        }
        return PrimeDatePicker.bottomSheetWith(CivilCalendar()).pickRangeDays(callback)
            .initiallyPickedStartDay(CivilCalendar(), pickEndDay = false).build()
    }

    override fun timePickerListener(
        view: TimePicker, hourOfDay: Int, minute: Int
    ) {
        binding.tvTimeEnd.text = getString(R.string.time_picker, hourOfDay, minute)
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityAddTaskBinding =
        ActivityAddTaskBinding.inflate(inflater)
}