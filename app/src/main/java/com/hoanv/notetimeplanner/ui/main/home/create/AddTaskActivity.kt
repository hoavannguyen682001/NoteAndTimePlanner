package com.hoanv.notetimeplanner.ui.main.home.create

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.widget.TimePicker
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.asFlow
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primedatepicker.common.BackgroundShapeType
import com.aminography.primedatepicker.common.LabelFormatter
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.RangeDaysPickCallback
import com.aminography.primedatepicker.picker.theme.LightThemeFactory
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Attach
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.FileInfo
import com.hoanv.notetimeplanner.data.models.ImageInfo
import com.hoanv.notetimeplanner.data.models.SubTask
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.TypeTask
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.data.models.notification.DataTask
import com.hoanv.notetimeplanner.data.models.notification.MessageTask
import com.hoanv.notetimeplanner.data.models.notification.NotificationData
import com.hoanv.notetimeplanner.databinding.ActivityAddTaskBinding
import com.hoanv.notetimeplanner.databinding.DialogAddMemberBinding
import com.hoanv.notetimeplanner.databinding.DialogAddSubtaskBinding
import com.hoanv.notetimeplanner.service.ScheduledWorker.Companion.TASK_ID
import com.hoanv.notetimeplanner.service.boardcast.DownloadManagerReceiver
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.evenbus.CheckReloadListTask
import com.hoanv.notetimeplanner.ui.main.home.create.adapter.CategoryAdapter
import com.hoanv.notetimeplanner.ui.main.home.create.adapter.FileAttachAdapter
import com.hoanv.notetimeplanner.ui.main.home.create.adapter.ImageAttachAdapter
import com.hoanv.notetimeplanner.ui.main.home.create.adapter.MemberAdapter
import com.hoanv.notetimeplanner.ui.main.home.create.adapter.SubTaskAdapter
import com.hoanv.notetimeplanner.ui.main.home.create.dialog.TimePickerFragment
import com.hoanv.notetimeplanner.utils.FieldValidators
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectIn
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import com.hoanv.notetimeplanner.utils.extension.visible
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

    private lateinit var dialogMemberBinding: DialogAddMemberBinding
    private lateinit var alertMemberDialog: AlertDialog

    private val categoryAdapter by lazy {
        CategoryAdapter(this) { category, position ->
            selectedS.tryEmit(position)
        }
    }

    private val categoryList = mutableListOf<Category>()

    private val memBerAdapter by lazy {
        MemberAdapter(this) {
            if (mTask.userId == Pref.userId) {
                mListMember.remove(it)
                _listMember = mListMember
            } else {
                toastError("Bạn không có quyền xoá thành viên này!")
            }
        }
    }

    private val subTaskAdapter by lazy {
        SubTaskAdapter(this, ::onIconCheckSubTaskClick, ::onDeleteSubTaskClick)
    }

    private val imageAttachAdapter by lazy {
        ImageAttachAdapter(this,
            {
                binding.flImagePreview.visible()
                Glide.with(this)
                    .load(it.imageUrl)
                    .into(binding.layoutPreview.ivPreview)
            },
            {
                mListImage.remove(it)
                listImageS = mListImage
            })
    }

    private val fileAttachAdapter by lazy {
        FileAttachAdapter(this,
            {
                downloadFile(it)
            },
            {
                mListFile.remove(it)
                listFileS = mListFile
            })
    }

    private var selectedS = MutableStateFlow(0)

    /* Trigger list member */
    private var listMemberTriggerS = MutableSharedFlow<List<UserInfo>>(extraBufferCapacity = 64)
    private var mListMember = mutableListOf<UserInfo>()
    private var _listMember = listOf<UserInfo>()
        set(value) {
            field = value
            listMemberTriggerS.tryEmit(value)
        }

    /* Trigger list sub task */
    private var listSubTaskS = MutableSharedFlow<List<SubTask>>(extraBufferCapacity = 64)
    private var mListSubTask = mutableListOf<SubTask>()
    private var _listSubTask = listOf<SubTask>()
        set(value) {
            field = value
            listSubTaskS.tryEmit(value)
        }

    /* Trigger list image */
    private val listImageUri = mutableListOf<ImageInfo>()
    private var listImageTriggerS = MutableSharedFlow<List<ImageInfo>>(extraBufferCapacity = 64)
    private var mListImage = mutableListOf<ImageInfo>()
    private var listImageS = listOf<ImageInfo>()
        set(value) {
            field = value
            listImageTriggerS.tryEmit(value)
        }
    private lateinit var registerImagePicker: ActivityResultLauncher<PickVisualMediaRequest>

    /* Trigger list file doc */
    private val listFileUri = mutableListOf<FileInfo>()
    private var listFileTriggerS = MutableSharedFlow<List<FileInfo>>(extraBufferCapacity = 64)
    private var mListFile = mutableListOf<FileInfo>()
    private var listFileS = listOf<FileInfo>()
        set(value) {
            field = value
            listFileTriggerS.tryEmit(value)
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
    private var mTask: Task = Task()
    private var isLoadUser = true
    private var isFirstLoad = true

    /* set up time for schedule notification */
    private val timeNotification = object : TimePickerFragment.TimePickerListener {
        override fun timePickerListener(view: TimePicker, hourOfDay: Int, minute: Int) {
            binding.run {
                tvTimeNotification.text =
                    getString(
                        R.string.time_picker,
                        "$hourOfDay",
                        if (minute < 10) "0" else "",
                        "$minute"
                    )

                val timeNoti = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                    tvTimeNotification.text.toString()
                )

                val timeEnd = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                    tvTimeEnd.text.toString()
                )

                if (timeNoti != null) {
                    if (timeNoti.after(timeEnd)) {
                        toastError("Vui lòng chọn thời gian thông báo trước thời gian kết thúc!")
                        tvTimeNotification.text = getString(R.string.pick_time)
                    }
                }
            }
        }
    }

    private var registerPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            turnOnPushNotification()
        } else {
            turnOffPushNotification()
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
            cslDetailTask.gone()

            /* id from task list */
            val task = intent.getParcelableExtra<Task>("TODO")
            task?.let {
                idTodo = it.id
                viewModel.getDetailTask(it.id)
//                loadDataView(it)
            }

            /* id from notification */
            val taskId = intent.getStringExtra(TASK_ID)
            taskId?.let {
                idTodo = it
                viewModel.getDetailTask(it)
            }

            if (task == null && taskId == null) {
                tvTaskPersonal.isSelected = true
                tvStartDay.text = currentDay
                tvEndDay.text = currentDay
                tvTimeEnd.text = currentTime
                swcNotification.isChecked = false
                tvTimeNotification.isEnabled = false
                viewModel.getListCategory()
            }

            rvListCategory.run {
                adapter = categoryAdapter
                layoutManager = LinearLayoutManager(
                    this@AddTaskActivity, LinearLayoutManager.HORIZONTAL, false
                )
                itemAnimator = null
            }

            rvListMember.run {
                layoutManager = LinearLayoutManager(
                    this@AddTaskActivity, LinearLayoutManager.HORIZONTAL, false
                )
                adapter = memBerAdapter
            }

            rvAddSubTask.run {
                layoutManager = LinearLayoutManager(
                    this@AddTaskActivity, LinearLayoutManager.VERTICAL, false
                )
                adapter = subTaskAdapter
                isNestedScrollingEnabled = false
                itemAnimator = null
            }

            rvAttachImage.run {
                layoutManager = LinearLayoutManager(
                    this@AddTaskActivity, LinearLayoutManager.HORIZONTAL, false
                )
                adapter = imageAttachAdapter
            }

            rvAttachFile.run {
                layoutManager = LinearLayoutManager(
                    this@AddTaskActivity, LinearLayoutManager.VERTICAL, false
                )
                adapter = fileAttachAdapter
            }

            /* Dialog add subtask */
            dialogBinding =
                DialogAddSubtaskBinding.inflate(LayoutInflater.from(this@AddTaskActivity))
            alertDialog =
                AlertDialog.Builder(this@AddTaskActivity, R.style.AppCompat_AlertDialog)
                    .setView(dialogBinding.root)
                    .setCancelable(false).create()

            registerImagePicker =
                registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri != null) {

                        val imageInfo = ImageInfo(
                            imageUrl = uri.toString()
                        )
                        /* add uri to list image for upload image of task */
                        listImageUri.add(imageInfo)

                        mListImage.add(imageInfo)
                        listImageS = mListImage

                        Log.d("listImageS", "$listImageS")
                    }
                }

            dialogMemberBinding =
                DialogAddMemberBinding.inflate(LayoutInflater.from(this@AddTaskActivity))
            alertMemberDialog =
                AlertDialog.Builder(this@AddTaskActivity, R.style.AppCompat_AlertDialog)
                    .setView(dialogMemberBinding.root)
                    .setCancelable(false).create()
        }
    }

    private fun initListener() {
        binding.run {
            btnClose.setOnSingleClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            layoutPreview.ivClosePreview.setOnSingleClickListener {
                Glide.with(this@AddTaskActivity).clear(layoutPreview.ivPreview)
                flImagePreview.gone()
            }

            edtDescription.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    edtDescription.clearFocus()
                }
            }

            edtTitle.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    edtTitle.clearFocus()
                }
            }

            tvTaskPersonal.setOnClickListener {
                typeTask = TypeTask.PERSONAL
                tvAddMember.gone()
                rvListMember.gone()
                tvMember.gone()

                mListMember.clear()
                _listMember = mListMember
                isLoadUser = true

                tvTaskPersonal.run {
                    isSelected = true
                    tvTaskPersonal.setTextColor(resourceColor(R.color.white))
                }
                tvTaskGroup.run {
                    isSelected = false
                    setTextColor(resourceColor(R.color.arsenic))
                }
            }

            tvTaskGroup.setOnClickListener {
                typeTask = TypeTask.GROUP
                if (isLoadUser) {
                    viewModel.getUserInfo(Pref.userEmail)
                    isLoadUser = false
                }

                tvAddMember.visible()
                rvListMember.visible()
                tvMember.visible()

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

            tvAddMember.setOnSingleClickListener {
                dialogAddMember()
            }

            tvAddSubTask.setOnSingleClickListener {
                dialogAddSubTask()
            }

            tvAttachFile.setOnClickListener {
                handleOptionMenu(tvAttachFile)
            }

            btnSubmit.setOnSingleClickListener {
                if (edtTitle.text.isNullOrEmpty() || edtDescription.text.isNullOrEmpty()) {
                    toastError("Vui lòng điền đầy đủ tiêu đề và mô tả")
                } else {
                    showLoadingDialog()
                    if (idTodo.isNullOrEmpty()) {
                        addTask()
                    } else {
                        updateTask()
                    }
                }
            }

            swcNotification.setOnCheckedChangeListener { _, isChecked ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkPermission()
                } else {
                    if (isChecked) {
                        turnOnPushNotification()
                    } else {
                        turnOffPushNotification()
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
                    .combine(listCategory.asFlow()) { selected, list ->
                        Pair(selected, list)
                    }
                    .collectIn(this@AddTaskActivity) { item ->
                        val (select, state) = item
                        if (idTodo == null) {
                            val listCate = mutableListOf<Category>()
                            listCate.addAll(state)
                            listCate.mapIndexed { index, category ->
                                category.isSelected = index == select
                                mCategory = listCate[select]
                            }
                            categoryAdapter.submitList(listCate.map { it.ownCopy() })
                        } else {
                            if (isFirstLoad) {
                                val listCate = mutableListOf<Category>()
                                var i = 0
                                listCate.addAll(state)
                                listCate.mapIndexed { index, category ->
                                    category.isSelected = mTask.category.id == category.id
                                    mCategory = mTask.category
                                    if (mTask.category.id == category.id) {
                                        i = index
                                    }
                                }
                                categoryAdapter.submitList(listCate.map { it.ownCopy() }) {
                                    rvListCategory.scrollToPosition(i)
                                }
                                isFirstLoad = false
                            } else {
                                val listCate = mutableListOf<Category>()
                                listCate.addAll(state)
                                listCate.mapIndexed { index, category ->
                                    category.isSelected = index == select
                                    mCategory = listCate[select]
                                }
                                categoryAdapter.submitList(listCate.map { it.ownCopy() })
                            }
                        }
                        cslDetailTask.visible()
                        lottieAnim.gone()
                    }

                addTaskTriggerS.observe(this@AddTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            toastSuccess(state.data)
                            EventBus.getDefault().post(CheckReloadListTask(true))
                            dismissLoadingDialog()
                            finish()
                        }

                        is ResponseState.Failure -> {
                            dismissLoadingDialog()
                            toastError(state.throwable?.message)
                        }
                    }
                }

                sendNotiTriggerS.collectIn(this@AddTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
//                            toastSuccess(state.data.name)
//                            finish()
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
                            dismissLoadingDialog()
                            finish()
                        }

                        is ResponseState.Failure -> {
                            dismissLoadingDialog()
                            toastError(state.throwable?.message)
                        }
                    }
                }

                detailTask.observe(this@AddTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                            cslDetailTask.gone()
                            lottieAnim.visible()
                        }

                        is ResponseState.Success -> {
                            loadDataView(state.data)
                            getListCategory()
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
                }

                uploadImageTriggerS.observe(this@AddTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            if (listFileUri.isNotEmpty()) {
                                viewModel.uploadFileOfTask(state.data, listFileUri)
                            } else {
                                if (idTodo.isNullOrEmpty()) {
                                    viewModel.addNewTask(state.data)
                                    mCategory.listTask++
                                    viewModel.updateCategory(mCategory, "listTask")
                                } else {
                                    viewModel.updateTask(state.data)
                                }
                            }
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
                }

                uploadFileTriggerS.observe(this@AddTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            if (idTodo.isNullOrEmpty()) {
                                viewModel.addNewTask(state.data)
                                mCategory.listTask++
                                viewModel.updateCategory(mCategory, "listTask")
                            } else {
                                viewModel.updateTask(state.data)
                            }
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
                }

                userInfo.observe(this@AddTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            mListMember.add(state.data)
                            if (idTodo != null) {
                                _listMember = mListMember.sortedWith(compareByDescending {
                                    it.uid == mTask.userId
                                })
                            } else {
                                _listMember = mListMember
                            }
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
                }

                listUserInfo.observe(this@AddTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            state.data.forEach {
                                mListMember.add(it)
                            }
                            _listMember = mListMember.sortedWith(compareByDescending {
                                it.uid == mTask.userId
                            })
                            Log.d("mListMember", "${mListMember}")
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
                }
            }

            /* collect list member */
            listMemberTriggerS.collectIn(this@AddTaskActivity) { list ->
                memBerAdapter.submitList(list.map { it.copy() })
            }

            /* collect list sub task */
            listSubTaskS.collectIn(this@AddTaskActivity) { list ->
                subTaskAdapter.submitList(list.map { it.ownCopy() })
            }

            /* collect list image attach */
            listImageTriggerS.collectIn(this@AddTaskActivity) { list ->
                imageAttachAdapter.submitList(list.map { it.copy() })
            }

            /* collect list file attach*/
            listFileTriggerS.collectIn(this@AddTaskActivity) { list ->
                fileAttachAdapter.submitList(list.map { it.copy() })
                Log.d("fileAttachAdapter", "$list")
            }
        }
    }

    private val registerGetFileDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let { uri ->
                val documentFile = DocumentFile.fromSingleUri(this@AddTaskActivity, uri)
                documentFile?.let {
                    val fileInfo = FileInfo(
                        title = documentFile.name ?: "",
                        fileUrl = documentFile.uri.toString()
                    )

                    listFileUri.add(fileInfo)
                    mListFile.add(fileInfo)
                    listFileS = mListFile
                    Log.d("documentFile", "${listFileS}")
                }
            }
        }

    private fun getFileDocument() {
        val typeFile = arrayOf(
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )

        registerGetFileDocument.launch(typeFile)
    }

    private fun downloadFile(fileInfo: FileInfo) {
        val request = DownloadManager.Request(Uri.parse(fileInfo.fileUrl))
        request.setTitle("Tải xuống ${fileInfo.title}")
        request.setDescription("Tệp tin đang được tải xuống")

        // Thiết lập đường dẫn lưu trữ của tệp tin
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileInfo.title)

        // Đặt những cờ cho request
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // Lấy DownloadManager và gửi yêu cầu tải về
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Optional: Lắng nghe sự kiện khi tải về hoàn tất
        val onCompleteReceiver = DownloadManagerReceiver(downloadId)
        ContextCompat.registerReceiver(
            this@AddTaskActivity,
            onCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun turnOnPushNotification() {
        binding.swcNotification.isChecked = true
        binding.tvTimeNotification.run {
            text = binding.tvTimeEnd.text
            if (!idTodo.isNullOrEmpty()) {
                if (!mTask.scheduledTime.isNullOrEmpty()) {
                    text = mTask.scheduledTime
                }
            }
            isEnabled = true
            setTextColor(resourceColor(R.color.black))
            backgroundTintList = getColorStateList(R.color.white)
        }
    }

    private fun turnOffPushNotification() {
        binding.swcNotification.isChecked = false
        binding.tvTimeNotification.run {
            text = "Chọn thời gian"
            isEnabled = false
            setTextColor(resourceColor(R.color.dark_gray))
            backgroundTintList = getColorStateList(R.color.cultured)
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

            categoryList.mapIndexed { index, category ->
                if (category.id == mTask.category.id) {
                    selectedS.tryEmit(index)
                }
            }

            if (!mTask.scheduledTime.isNullOrEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@AddTaskActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        turnOnPushNotification()
                    } else {
                        turnOffPushNotification()
                    }
                } else {
                    turnOnPushNotification()
                }
            } else {
                turnOffPushNotification()
            }

            if (task.typeTask == TypeTask.GROUP) {
                typeTask = TypeTask.GROUP
                isLoadUser = false // user info being loading
                tvAddMember.visible()
                rvListMember.visible()
                tvMember.visible()

                tvTaskGroup.run {
                    isSelected = true
                    setTextColor(resourceColor(R.color.white))
                }
                tvTaskPersonal.run {
                    isEnabled = false
                    isSelected = false
                    setTextColor(resourceColor(R.color.arsenic))
                }
            } else {
                typeTask = TypeTask.PERSONAL
                tvAddMember.gone()
                rvListMember.gone()
                tvMember.gone()
                tvTaskPersonal.run {
                    isSelected = true
                    tvTaskPersonal.setTextColor(resourceColor(R.color.white))
                }
                tvTaskGroup.run {
                    isSelected = false
                    setTextColor(resourceColor(R.color.arsenic))
                }
            }

            if (mTask.listMember.isNotEmpty()) {
                viewModel.getListUserInfo(mTask.listMember)
            }

            if (mTask.subTask.isNotEmpty()) {
                mListSubTask.addAll(mTask.subTask)
                subTaskAdapter.submitList(mTask.subTask.map { it.ownCopy() })
            }

            if (mTask.attachFile.listImage.isNotEmpty()) {
                mListImage.addAll(mTask.attachFile.listImage)
                imageAttachAdapter.submitList(mTask.attachFile.listImage.map { it.copy() })
            }

            if (mTask.attachFile.listFile.isNotEmpty()) {
                mListFile.addAll(mTask.attachFile.listFile)
                fileAttachAdapter.submitList(mTask.attachFile.listFile.map { it.copy() })
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
                scheduledTime = if (swcNotification.isChecked) tvTimeNotification.text.toString() else null,
                subTask = mListSubTask,
                listMember = mListMember,
                attachFile = Attach(
                    listImage = mListImage,
                    listFile = mListFile
                ),
                taskState = false,
                typeTask = typeTask
            )

            if (swcNotification.isChecked) {
                isUpdate = false
                setScheduledTime(task, isSchedule = true, isUpdate)
            }

            if (listImageUri.isNotEmpty()) {
                viewModel.uploadImageOfTask(task, listImageUri)
            } else {
                viewModel.addNewTask(task)
                mCategory.listTask++
                viewModel.updateCategory(mCategory, "listTask")
            }
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
                scheduledTime = if (swcNotification.isChecked) tvTimeNotification.text.toString() else null,
                subTask = mListSubTask,
                listMember = mListMember,
                attachFile = Attach(
                    listImage = mListImage,
                    listFile = mListFile
                ),
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

            if (task.typeTask == TypeTask.GROUP) {
                pushNotificationOfGroupTask(task)
            }

            if (listImageUri.isNotEmpty()) {
                viewModel.uploadImageOfTask(task, listImageUri)
            } else {
                viewModel.updateTask(task)
            }

            //TODO get detail category
//            if (mTask.category.id != mCategory.id) {
//                viewModel.updateCategory(mCategory, "listTask")
//
//                mTask.category.listTask--
//                viewModel.updateCategory(mTask.category, "listTask")
//            }
        }
    }

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
         * Setup object to set schedule time for notification
         */
        if (task.typeTask == TypeTask.GROUP) {
            if (task.listMember.isNotEmpty()) {
                task.listMember.forEach {
                    val data = DataTask(
                        taskId = task.id,
                        uniqueId = task.uniqueId.toString(),
                        title = "Bạn có dự án sắp đến hạn",
                        content = task.title.toString(),
                        isScheduled = "$isSchedule",
                        scheduledTime = scheduledTime,
                        isNotification = "false",
                        isUpdate = isUpdate.toString()
                    )

                    val messageTask = MessageTask(
                        token = it.userToken,
                        data = data
                    )

                    val notificationData = NotificationData(
                        message = messageTask
                    )

                    viewModel.sendNotification(notificationData)
                    Log.d("listMember", "${it.userToken}")
                }
            }
        } else {
            val data = DataTask(
                taskId = task.id,
                uniqueId = task.uniqueId.toString(),
                title = "Bạn có công việc sắp đến hạn",
                content = task.title.toString(),
                isScheduled = "$isSchedule",
                scheduledTime = scheduledTime,
                isNotification = "false",
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
    }

    private fun pushNotificationOfGroupTask(task: Task) {
        if (task.listMember.isNotEmpty()) {
            task.listMember.forEach {
                if (Pref.deviceToken != it.userToken) {
                    val data = DataTask(
                        taskId = task.id,
                        uniqueId = task.uniqueId.toString(),
                        title = "Dự án của bạn vừa có cập nhật mới",
                        content = task.title.toString(),
                        isScheduled = "false",
                        scheduledTime = "",
                        isNotification = "true",
                        isUpdate = "false"
                    )

                    val messageTask = MessageTask(
                        token = it.userToken,
                        data = data
                    )

                    val notificationData = NotificationData(
                        message = messageTask
                    )

                    viewModel.sendNotification(notificationData)
                }
            }
        }
    }

    private fun dialogAddMember() {
        var isExist = false
        binding.run {
            dialogMemberBinding.run {
                tvAdd.setOnSingleClickListener {
                    if (edtEmail.text.isNullOrEmpty() || !validateEmail()) {
                        validateEmail()
                    } else if (mListMember.isNotEmpty()) {
                        mListMember.forEach {
                            if (it.userEmail == dialogMemberBinding.edtEmail.text.toString()) {
                                toastError("Người này đã là thành viên!")
                                isExist = true
                                return@forEach
                            }
                        }
                        if (!isExist) {
                            viewModel.getUserInfo(edtEmail.text.toString())
                            edtEmail.text!!.clear()
                            alertMemberDialog.dismiss()
                        }
                    } else {
                        viewModel.getUserInfo(edtEmail.text.toString())
                        edtEmail.text!!.clear()
                        alertMemberDialog.dismiss()
                    }
                }
                tvCancel.setOnSingleClickListener {
                    edtEmail.text!!.clear()
                    alertMemberDialog.dismiss()
                }
            }
        }
        alertMemberDialog.show()
    }

    private fun validateEmail(): Boolean {
        dialogMemberBinding.run {
            val text = edtEmail.text.toString().trim()
            if (text.isEmpty() || !FieldValidators.isValidEmail(text)) {
                tilEmail.isErrorEnabled = true
                tilEmail.error = "Vui lòng nhập đúng format: example@gmail.com"
                return false
            } else {
                tilEmail.isErrorEnabled = false
            }
            return true
        }
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
                    edtTitle.clearFocus()
                    edtDescription.clearFocus()
                }
                tvCancel.setOnSingleClickListener {
                    edtTitleSubTask.text.clear()
                    edtTitle.clearFocus()
                    edtDescription.clearFocus()
                    alertDialog.dismiss()
                }
            }
        }
        alertDialog.show()
    }

    private fun onIconCheckSubTaskClick(item: SubTask) {
        val tempList = mListSubTask.map {
            if (it.taskId == item.taskId) {
                it.isDone = !it.isDone
            }
            it
        }
        _listSubTask = tempList
    }

    private fun onDeleteSubTaskClick(item: SubTask) {
        mListSubTask.remove(item)
        _listSubTask = mListSubTask
    }

    @SuppressLint("RestrictedApi")
    private fun handleOptionMenu(view: View) {
        val popupMenu = PopupMenu(this@AddTaskActivity, view)
        popupMenu.inflate(R.menu.attach_file)
        val popupHelper =
            MenuPopupHelper(this@AddTaskActivity, popupMenu.menu as MenuBuilder, view)
        popupHelper.setForceShowIcon(true)


        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemImage -> {
                    registerImagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                R.id.itemFile -> {
                    getFileDocument()
                }
            }
            false
        }

        popupHelper.show()
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (binding.swcNotification.isChecked) {
                turnOnPushNotification()
            } else {
                turnOffPushNotification()
            }
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                toastWarning("Bạn đã từ chối quyền thông báo. Vui lòng vào cài đặt để cho phép ứng dụng hiển thị thông báo!")
            } else {
                registerPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            turnOffPushNotification()
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityAddTaskBinding =
        ActivityAddTaskBinding.inflate(inflater)
}