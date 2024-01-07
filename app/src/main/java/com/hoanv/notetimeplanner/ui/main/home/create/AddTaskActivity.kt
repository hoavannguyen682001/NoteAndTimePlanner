package com.hoanv.notetimeplanner.ui.main.home.create

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TimePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.asFlow
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primedatepicker.common.BackgroundShapeType
import com.aminography.primedatepicker.common.LabelFormatter
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.RangeDaysPickCallback
import com.aminography.primedatepicker.picker.theme.LightThemeFactory
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.notification.DataTask
import com.hoanv.notetimeplanner.data.models.notification.MessageTask
import com.hoanv.notetimeplanner.data.models.notification.NotificationData
import com.hoanv.notetimeplanner.databinding.ActivityAddTaskBinding
import com.hoanv.notetimeplanner.databinding.DialogCategoryBinding
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.TASK_ID
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.home.create.adapter.CategoryAdapter
import com.hoanv.notetimeplanner.ui.main.home.create.dialog.TimePickerFragment
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectIn
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AddTaskActivity : BaseActivity<ActivityAddTaskBinding, AddTaskVM>(),
    TimePickerFragment.TimePickerListener {
    override val viewModel: AddTaskVM by viewModels()

    private val timePickerFrag = TimePickerFragment()

    private val categoryAdapter by lazy {
        CategoryAdapter(this) { category, position ->
            selectedS.tryEmit(position)
        }
    }

    private var selectedS = MutableStateFlow(0)

    private lateinit var dialogBinding: DialogCategoryBinding
    private lateinit var alertDialog: AlertDialog

    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val date = Date()
    private var currentDay = formatter.format(date)
    private val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    private var idTodo: String? = null

    private lateinit var mCategory: Category
    private var endDay = CivilCalendar()
    private var startDay = CivilCalendar()


    private val items = arrayOf("Trước 5 phút", "Trước 15 phút", "Trước 1 ngày")

    override fun init(savedInstanceState: Bundle?) {
        timePickerFrag.setDataTimePicker(this@AddTaskActivity)
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.run {
            val task = intent.getParcelableExtra<Task>("TODO")
            val taskId = intent.getStringExtra(TASK_ID)
            task?.let {
                idTodo = it.id
                loadDataView(it)
            }
            taskId?.let {
                idTodo = it
                viewModel.getDetailTask(it)
            }

            if (task == null && taskId == null) {
                tvStartDay.text = currentDay
                tvEndDay.text = currentDay
                tvTimeEnd.text = currentTime
            }

            rvListCategory.run {
                adapter = categoryAdapter
                layoutManager = LinearLayoutManager(
                    this@AddTaskActivity, LinearLayoutManager.HORIZONTAL, false
                )
                itemAnimator = null
            }

            val adapter =
                ArrayAdapter(this@AddTaskActivity, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnNotification.adapter = adapter

        }
    }

    private fun initListener() {
        binding.run {
            ivClose.setOnSingleClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            spnNotification.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Xử lý khi một item được chọn
                    val selectedItem = parent.getItemAtPosition(position).toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

            tvStartDay.setOnSingleClickListener {
                dateRangePicker().show(supportFragmentManager, "DatePicker")
            }

            tvEndDay.setOnSingleClickListener {
                dateRangePicker().show(supportFragmentManager, "DatePicker")
            }

            tvTimeEnd.setOnSingleClickListener {
                timePickerFrag.show(supportFragmentManager, "TimerPicker")
            }

            ivSubmit.setOnSingleClickListener {
                addTask()
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {

                selectedS
                    .combine(listCategory.asFlow()) { selected, list -> Pair(selected, list) }
                    .collectIn(this@AddTaskActivity) { item ->
                        val (select, state) = item
                        val listCate = mutableListOf<Category>()
                        listCate.add(0, Category(title = "Không thể loại"))

                        listCate.addAll(state)
                        listCate.mapIndexed { index, category ->
                            category.isSelected = index == select
                            mCategory = listCate[select]
                        }
                        categoryAdapter.submitList(listCate.map { it.ownCopy() })
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

                sendNotiTriggerS.collectIn(this@AddTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            toastSuccess(state.data.name)
                            finish()
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
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

                detailTask.observe(this@AddTaskActivity) {
                    loadDataView(it)
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
//            if (!idTodo.isNullOrEmpty()) {
//                task.id = idTodo!!
//                viewModel.updateTask(task)
//            } else {
//                viewModel.addNewTask(task)
//                mCategory.listTask++
//                viewModel.updateCategory(mCategory, "listTask")
//            }

            val scheduledTime = "${task.endDay} ${task.timeEnd}:00"

            /**
             * Setup object to send notification
             */
            val data = DataTask(
                taskId = task.id,
                title = "Bạn có công việc sắp đến hạn",
                content = task.title.toString(),
                isScheduled = "true",
                scheduledTime = scheduledTime
            )
            val messageTask = MessageTask(
                token = Pref.deviceToken,
                data = data
            )

            val notificationData = NotificationData(
                message = messageTask
            )

            viewModel.sendNotification(notificationData)
        }
    }

    private val themeFactory = object : LightThemeFactory() {

        override val typefacePath: String
            get() = "fonts/righteous_regular.ttf"

        override val dialogBackgroundColor: Int
            get() = getColor(R.color.yellow100)

        override val calendarViewBackgroundColor: Int
            get() = getColor(R.color.yellow100)

        override val pickedDayBackgroundShapeType: BackgroundShapeType
            get() = BackgroundShapeType.ROUND_SQUARE

        override val calendarViewPickedDayBackgroundColor: Int
            get() = getColor(R.color.green800)

        override val calendarViewPickedDayInRangeBackgroundColor: Int
            get() = getColor(R.color.green400)

        override val calendarViewPickedDayInRangeLabelTextColor: Int
            get() = getColor(R.color.gray900)

        override val calendarViewTodayLabelTextColor: Int
            get() = getColor(R.color.purple200)

        override val calendarViewWeekLabelFormatter: LabelFormatter
            get() = { primeCalendar ->
                when (primeCalendar[Calendar.DAY_OF_WEEK]) {
                    Calendar.SATURDAY,
                    Calendar.SUNDAY -> String.format("%s😍", primeCalendar.weekDayNameShort)

                    else -> String.format("%s", primeCalendar.weekDayNameShort)
                }
            }

        override val calendarViewWeekLabelTextColors: SparseIntArray
            get() = SparseIntArray(7).apply {
                val red = getColor(R.color.red300)
                val indigo = getColor(R.color.indigo500)
                put(Calendar.SATURDAY, red)
                put(Calendar.SUNDAY, red)
                put(Calendar.MONDAY, indigo)
                put(Calendar.TUESDAY, indigo)
                put(Calendar.WEDNESDAY, indigo)
                put(Calendar.THURSDAY, indigo)
                put(Calendar.FRIDAY, indigo)
            }

        override val calendarViewShowAdjacentMonthDays: Boolean
            get() = true

        override val selectionBarBackgroundColor: Int
            get() = getColor(R.color.brown600)

        override val selectionBarRangeDaysItemBackgroundColor: Int
            get() = getColor(R.color.orange700)
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
                startDay = str.toCivil()

                tvEndDay.text = getString(
                    R.string.date_range_selected,
                    end.date,
                    end.month + 1,
                    end.year
                )
                endDay = end.toCivil()
            }
        }
        val primeDatePicker = PrimeDatePicker.Companion
            .dialogWith(CivilCalendar())
            .pickRangeDays(callback)
        primeDatePicker.applyTheme(themeFactory)
        primeDatePicker.initiallyPickedRangeDays(startDay, endDay)

        return primeDatePicker.build()
    }

    override fun timePickerListener(
        view: TimePicker, hourOfDay: Int, minute: Int
    ) {
        binding.tvTimeEnd.text = getString(R.string.time_picker, hourOfDay, minute)
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityAddTaskBinding =
        ActivityAddTaskBinding.inflate(inflater)
}