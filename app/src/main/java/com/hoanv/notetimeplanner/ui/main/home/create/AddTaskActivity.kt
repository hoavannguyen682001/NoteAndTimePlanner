package com.hoanv.notetimeplanner.ui.main.home.create

import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
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
import com.hoanv.notetimeplanner.data.models.NotificationInfo
import com.hoanv.notetimeplanner.data.models.SubTask
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.TypeTask
import com.hoanv.notetimeplanner.data.models.notification.DataTask
import com.hoanv.notetimeplanner.data.models.notification.MessageTask
import com.hoanv.notetimeplanner.data.models.notification.NotificationData
import com.hoanv.notetimeplanner.databinding.ActivityAddTaskBinding
import com.hoanv.notetimeplanner.databinding.DialogAddCategoryBinding
import com.hoanv.notetimeplanner.databinding.DialogAddSubtaskBinding
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.TASK_ID
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.evenbus.CheckReloadListTask
import com.hoanv.notetimeplanner.ui.main.home.create.adapter.CategoryAdapter
import com.hoanv.notetimeplanner.ui.main.home.create.adapter.SubTaskAdapter
import com.hoanv.notetimeplanner.ui.main.home.create.dialog.TimePickerFragment
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectIn
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import fxc.dev.common.extension.resourceColor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AddTaskActivity : BaseActivity<ActivityAddTaskBinding, AddTaskVM>(),
    TimePickerFragment.TimePickerListener {
    override val viewModel: AddTaskVM by viewModels()

    private val timePickerFrag = TimePickerFragment()
    private val timeNotiPicker = TimePickerFragment()

    private lateinit var dialogBinding: DialogAddSubtaskBinding
    private lateinit var alertDialog: AlertDialog

    private val categoryAdapter by lazy {
        CategoryAdapter(this) { category, position ->
            selectedS.tryEmit(position)
        }
    }

    private val subTaskAdapter by lazy {
        SubTaskAdapter(this) {}
    }

    private var selectedS = MutableStateFlow(0)

    private var listSubTaskS = MutableSharedFlow<List<SubTask>>(extraBufferCapacity = 64)
    private var mListSubTask = mutableListOf<SubTask>()
    private var _listSubTask = listOf<SubTask>()
        set(value) {
            field = value
            listSubTaskS.tryEmit(value)
        }

    /* inti current day and time */
    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val date = Date()
    private var currentDay = formatter.format(date)
    private val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    /* Id task when */
    private var idTodo: String? = null

    private lateinit var mCategory: Category

    /* inti day for calendar */
    private var endDay = CivilCalendar()
    private var startDay = CivilCalendar()

    /* flag to check property in object task */
    private var isUpdate = false
    private var typeTask = TypeTask.PERSONAL
    private lateinit var mTask: Task

    /* set up time for schedule notification */
    private val timeNotification = object : TimePickerFragment.TimePickerListener {
        override fun timePickerListener(view: TimePicker, hourOfDay: Int, minute: Int) {
            binding.tvTimeNotification.text =
                getString(
                    R.string.time_picker,
                    "$hourOfDay",
                    if (minute < 10) "0" else "",
                    "$minute"
                )

            val timeNoti = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                binding.tvTimeNotification.text.toString()
            )

            val timeEnd = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                binding.tvTimeEnd.text.toString()
            )

            if (timeNoti != null) {
                if (timeNoti > timeEnd) {
                    toastError("Vui lòng chọn thời gian thông báo trước thời gian kết thúc!")
                    binding.tvTimeNotification.text = getString(R.string.pick_time)
                }
            }
        }
    }

    override fun init(savedInstanceState: Bundle?) {
        timePickerFrag.setDataTimePicker(this@AddTaskActivity)
        timeNotiPicker.setDataTimePicker(timeNotification)
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.run {
            /* id from task list */
            val task = intent.getParcelableExtra<Task>("TODO")
            task?.let {
                idTodo = it.id
                loadDataView(it)
            }

            /* id from notification */
            val taskId = intent.getStringExtra(TASK_ID)
            taskId?.let {
                idTodo = it
                viewModel.getDetailTask(it)
            }

            tvTaskPersonal.isSelected = true
            if (task == null && taskId == null) {
                tvStartDay.text = currentDay
                tvEndDay.text = currentDay
                tvTimeEnd.text = currentTime
                swcNotification.isChecked = false
                tvTimeNotification.isEnabled = false
            }

            rvListCategory.run {
                adapter = categoryAdapter
                layoutManager = LinearLayoutManager(
                    this@AddTaskActivity, LinearLayoutManager.HORIZONTAL, false
                )
                itemAnimator = null
            }

            rvAddSubTask.run {
                adapter = subTaskAdapter
                layoutManager =
                    LinearLayoutManager(this@AddTaskActivity, LinearLayoutManager.VERTICAL, false)
            }

            dialogBinding =
                DialogAddSubtaskBinding.inflate(LayoutInflater.from(this@AddTaskActivity))
            alertDialog =
                AlertDialog.Builder(this@AddTaskActivity, R.style.AppCompat_AlertDialog)
                    .setView(dialogBinding.root)
                    .setCancelable(false).create()
        }
    }

    private fun initListener() {
        binding.run {
            btnClose.setOnSingleClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            tvTaskPersonal.setOnSingleClickListener {
                typeTask = TypeTask.PERSONAL
                tvTaskPersonal.run {
                    isSelected = true
                    tvTaskPersonal.setTextColor(resourceColor(R.color.white))
                }
                tvTaskGroup.run {
                    isSelected = false
                    setTextColor(resourceColor(R.color.arsenic))
                }
            }

            tvTaskGroup.setOnSingleClickListener {
                typeTask = TypeTask.GROUP
                tvTaskGroup.run {
                    isSelected = true
                    setTextColor(resourceColor(R.color.white))
                }
                tvTaskPersonal.run {
                    isSelected = false
                    setTextColor(resourceColor(R.color.arsenic))
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

            tvAddSubTask.setOnSingleClickListener {
                dialogAddSubTask()
            }

            btnSubmit.setOnSingleClickListener {
                if (idTodo.isNullOrEmpty()) {
                    addTask()
                } else {
                    updateTask()
                }
            }

            swcNotification.setOnCheckedChangeListener { _, isChecked ->
                swcNotification.isChecked = isChecked
                tvTimeNotification.isEnabled = isChecked

                if (isChecked) {
                    tvTimeNotification.run {
                        setTextColor(resourceColor(R.color.black))
                        backgroundTintList = getColorStateList(R.color.white)
                    }
                } else {
                    tvTimeNotification.run {
                        setTextColor(resourceColor(R.color.dark_gray))
                        backgroundTintList = getColorStateList(R.color.cultured)
                    }
                }
            }

            tvTimeNotification.setOnSingleClickListener {
                timeNotiPicker.show(supportFragmentManager, "TimerPicker")
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

                        listCate.addAll(state)
                        listCate.mapIndexed { index, category ->
                            category.isSelected = index == select
                            mCategory = listCate[select]
                        }
                        categoryAdapter.submitList(listCate.map { it.ownCopy() })
                    }

                addTaskTriggerS.observe(this@AddTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
//                            pbLoading.visible()
                        }

                        is ResponseState.Success -> {
//                            pbLoading.gone()
                            toastSuccess(state.data)
                            EventBus.getDefault().post(CheckReloadListTask(true))
                            finish()
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
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
                    when (state) {
                        ResponseState.Start -> {
//                            pbLoading.visible()
                        }

                        is ResponseState.Success -> {
//                            pbLoading.gone()
                            EventBus.getDefault().post(CheckReloadListTask(true))
                            toastSuccess(state.data)
                            finish()
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
                }

                detailTask.observe(this@AddTaskActivity) {
                    loadDataView(it)
                }
            }

            listSubTaskS.collectIn(this@AddTaskActivity) {
                subTaskAdapter.submitList(it)
                Log.d("mListSubTask", "$it")
            }
        }
    }

    private fun loadDataView(task: Task) {
        mTask = task
        binding.run {
            edtTitle.setText(task.title)
            edtDescription.setText(task.description)
            tvStartDay.text = task.startDay
            tvEndDay.text = task.endDay
            tvTimeEnd.text = task.timeEnd

            if (!mTask.scheduledTime.isNullOrEmpty()) {
                swcNotification.isChecked = true
                tvTimeNotification.run {
                    tvTimeNotification.text = mTask.scheduledTime
                    isEnabled = true
                    setTextColor(resourceColor(R.color.black))
                    backgroundTintList = getColorStateList(R.color.white)
                }
            }
        }
    }

    private fun addTask() {
        binding.run {
            val task = Task(
                userId = Pref.userId,
                title = edtTitle.text.toString(),
                description = edtDescription.text.toString(),
                category = mCategory,
                uniqueId = Calendar.getInstance().timeInMillis.toInt(),
                timeEnd = tvTimeEnd.text.toString(),
                startDay = tvStartDay.text.toString(),
                endDay = tvEndDay.text.toString(),
                scheduledTime = binding.tvTimeNotification.text.toString(),
                taskState = false,
                typeTask = typeTask
            )

            if (swcNotification.isChecked) {
                isUpdate = false
                setScheduledTime(task, isSchedule = true, isUpdate)
            }

            viewModel.addNewTask(task)
            mCategory.listTask++
            viewModel.updateCategory(mCategory, "listTask")
        }
    }

    private fun updateTask() {
        binding.run {
            val task = Task(
                id = idTodo!!,
                userId = mTask.userId,
                title = edtTitle.text.toString(),
                description = edtDescription.text.toString(),
                category = mCategory,
                uniqueId = mTask.uniqueId,
                timeEnd = tvTimeEnd.text.toString(),
                startDay = tvStartDay.text.toString(),
                endDay = tvEndDay.text.toString(),
                scheduledTime = binding.tvTimeNotification.text.toString(),
                taskState = false,
                typeTask = typeTask
            )

            if (!mTask.scheduledTime.isNullOrEmpty()) {// Task set scheduled time when created
                if (swcNotification.isChecked) {
                    isUpdate =
                        (mTask.endDay != task.endDay) || (mTask.scheduledTime != task.scheduledTime)
                    setScheduledTime(task, isSchedule = true, isUpdate)
                } else {
                    isUpdate = true
                    task.scheduledTime = null
                    setScheduledTime(task, isSchedule = false, isUpdate = isUpdate)
                }
            } else {
                if (swcNotification.isChecked) {
                    isUpdate = false
                    setScheduledTime(task, isSchedule = true, isUpdate)
                }
            }

            viewModel.updateTask(task)

            //TODO check category to increase or decrease list task
        }
    }

    //TODO request permission notification
    private fun setScheduledTime(task: Task, isSchedule: Boolean, isUpdate: Boolean) {
        val scheduledTime = "${task.endDay} ${binding.tvTimeNotification.text}:00"

        /* Set information notification */
//        val notificationInfo = NotificationInfo(
//            taskId = task.id,
//            uniqueId = mTask.uniqueId,
//            title = task.title.toString(),
//            content = task.title.toString(),
//            dayNotification = task.endDay.toString(),
//            timeNotification = binding.tvTimeNotification.text.toString()
//        )

        /**
         * Setup object to send notification
         */
        val data = DataTask(
            taskId = task.id,
            uniqueId = task.uniqueId.toString(),
            title = "Bạn có công việc sắp đến hạn",
            content = task.title.toString(),
            isScheduled = "$isSchedule",
            scheduledTime = scheduledTime,
            isUpdate = isUpdate.toString()
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

    private fun dialogAddSubTask() {
        binding.run {
            dialogBinding.run {
                tvSave.setOnSingleClickListener {
                    if (edtTitleSubTask.text.isNullOrEmpty()) {
                        toastError("Vui lòng nhập tiêu đề công việc!")
                    } else {
                        val subTask = SubTask(
                            title = edtTitleSubTask.text.toString()
                        )
                        mListSubTask.add(subTask)
                        _listSubTask = mListSubTask

                        edtTitleSubTask.text.clear()

                        alertDialog.dismiss()
                    }
                }
                tvCancel.setOnSingleClickListener {
                    alertDialog.dismiss()
                }
            }
        }
        alertDialog.show()
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
                    R.string.date_selected,
                    if (str.date < 10) "0${str.date}" else "${str.date}",
                    if (str.month < 9) "0${str.month + 1}" else "${str.month + 1}",
                    "${str.year}"
                )
                startDay = str.toCivil()

                tvEndDay.text = getString(
                    R.string.date_selected,
                    if (end.date < 10) "0${end.date}" else "${end.date}",
                    if (end.month < 9) "0${end.month + 1}" else "${end.month + 1}",
                    "${end.year}"
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
        binding.tvTimeEnd.text = getString(
            R.string.time_picker, "$hourOfDay",
            if (minute < 10) "0" else "",
            "$minute"
        )
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityAddTaskBinding =
        ActivityAddTaskBinding.inflate(inflater)
}