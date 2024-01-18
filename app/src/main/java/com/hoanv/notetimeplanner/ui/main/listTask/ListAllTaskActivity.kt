package com.hoanv.notetimeplanner.ui.main.listTask

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.TypeTask
import com.hoanv.notetimeplanner.databinding.ActivityListAllTaskBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.home.create.AddTaskActivity
import com.hoanv.notetimeplanner.utils.AppConstant.TASK_TYPE
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectIn
import com.hoanv.notetimeplanner.utils.extension.flow.collectInViewLifecycle
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import com.hoanv.notetimeplanner.utils.extension.visible
import com.hoanv.notetimeplanner.utils.widget.swipe.GestureManager
import dagger.hilt.android.AndroidEntryPoint
import fxc.dev.common.extension.resourceColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ListAllTaskActivity : BaseActivity<ActivityListAllTaskBinding, ListAllTaskVM>() {
    override val viewModel: ListAllTaskVM by viewModels()

    private val listTaskAdapter by lazy {
        ListTaskAdapter(this, ::onTaskClick)
    }

    private val listTaskGroupAdp by lazy {
        ListGroupTaskAdapter(this) {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra("TODO", it)
            startActivity(intent)
        }
    }

    private val _listTaskS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
    private val mListTaskS = mutableListOf<Task>()
    private var listTaskS = listOf<Task>()
        set(value) {
            field = value
            _listTaskS.tryEmit(value)
        }

    private val _listGroupTaskS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
    private val mListGroupTaskS = mutableListOf<Task>()
    private var listGroupTaskS = listOf<Task>()
        set(value) {
            field = value
            _listGroupTaskS.tryEmit(value)
        }

    private var taskType: String = TypeTask.PERSONAL.name
    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.run {
            taskType = intent.getStringExtra(TASK_TYPE) ?: "Personal"

            setSelected(IsSelect.TvAll)
            rvListTask.run {
                layoutManager = LinearLayoutManager(
                    this@ListAllTaskActivity, LinearLayoutManager.VERTICAL, false
                )
                adapter = listTaskAdapter
            }

            rvListTaskGroup.run {
                layoutManager = GridLayoutManager(this@ListAllTaskActivity, 2)
                adapter = listTaskGroupAdp
            }
        }
    }

    private fun initListener() {
        binding.run {
            tvAll.setOnSingleClickListener {
                setSelected(IsSelect.TvAll)
                viewModel.getListTask()
            }
            tvTodo.setOnSingleClickListener {
                setSelected(IsSelect.TvTodo)
                filterListTaskByStatus(false)
            }
            tvDone.setOnSingleClickListener {
                setSelected(IsSelect.TvDone)
                filterListTaskByStatus(true)
            }
            tvExpire.setOnSingleClickListener {
                setSelected(IsSelect.TvExpire)
                filterListTaskByDate()
            }

            svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        filterListTask(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        viewModel.getListTask()
                    }
                    return false
                }
            })

            ivBack.setOnSingleClickListener {
                finish()
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                listTaskPersonal.asFlow().collectIn(this@ListAllTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                            lottieAnim.visible()
                            rvListTask.gone()
                        }

                        is ResponseState.Success -> {
                            /* List personal task */
                            listTaskS = state.data
                            mListTaskS.clear()
                            mListTaskS.addAll(state.data)
                        }

                        is ResponseState.Failure -> {
                            lottieAnim.gone()
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listGroupTask.asFlow().collectIn(this@ListAllTaskActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                            rvListTaskGroup.gone()
                            lottieAnim.visible()
                        }

                        is ResponseState.Success -> {
                            /* List group task */
                            listGroupTaskS = state.data
                            mListGroupTaskS.clear()
                            mListGroupTaskS.addAll(state.data)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }

                }

                _listTaskS.collectIn(this@ListAllTaskActivity) { list ->
                    listTaskAdapter.submitList(list.map { it.ownCopy() }) {
                        if (taskType == TypeTask.PERSONAL.name) {
                            onItemSwipe(list.toMutableList(), rvListTask)
                            lifecycleScope.launch {
                                delay(500)
                                lottieAnim.gone()
                                rvListTask.visible()
                                rvListTask.scrollToPosition(0)
                            }
                        }
                    }
                }

                _listGroupTaskS.collectIn(this@ListAllTaskActivity) { list ->
                    listTaskGroupAdp.submitList(list.map { it.ownCopy() }) {
                        if (taskType == TypeTask.GROUP.name) {
                            lifecycleScope.launch {
                                delay(500)
                                lottieAnim.gone()
                                rvListTaskGroup.visible()
                                rvListTaskGroup.scrollToPosition(0)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun filterListTask(title: String) {
        when (taskType) {
            TypeTask.PERSONAL.name -> {
                val tempList = listTaskS.filter {
                    if (it.title != null) {
                        it.title!!.contains(title)
                    } else {
                        false
                    }
                }

                listTaskS = tempList
            }

            TypeTask.GROUP.name -> {
                val tempList = listGroupTaskS.filter {
                    if (it.title != null) {
                        it.title!!.contains(title)
                    } else {
                        false
                    }
                }

                listGroupTaskS = tempList
            }
        }
    }

    private fun filterListTaskByStatus(state: Boolean) {
        when (taskType) {
            TypeTask.PERSONAL.name -> {
                val tempList = mListTaskS.filter {
                    if (state) {
                        it.taskState
                    } else {
                        if (!checkExpireDay(it)) {
                            !it.taskState
                        } else {
                            false
                        }
                    }
                }
                listTaskS = tempList
            }

            TypeTask.GROUP.name -> {
                val tempList = mListGroupTaskS.filter {
                    if (state) {
                        it.taskState
                    } else {
                        if (!checkExpireDay(it)) {
                            !it.taskState
                        } else {
                            false
                        }
                    }
                }
                listGroupTaskS = tempList
            }
        }
    }

    private fun filterListTaskByDate() {
        when (taskType) {
            TypeTask.PERSONAL.name -> {
                val tempList = mListTaskS.filter {
                    if (it.taskState) {
                        false
                    } else {
                        checkExpireDay(it)
                    }
                }
                listTaskS = tempList
            }

            TypeTask.GROUP.name -> {
                val tempList = mListGroupTaskS.filter {
                    if (it.taskState) {
                        false
                    } else {
                        checkExpireDay(it)
                    }
                }
                listGroupTaskS = tempList
            }
        }
    }

    private fun checkExpireDay(task: Task): Boolean {
        val endDay = task.endDay?.let { day ->
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(
                day
            )
        }
        val timeEnd = task.timeEnd?.let { time ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                time
            )
        }/* Check if end day before today or if timeEnd before current time */
        return (Date() == endDay && Date().after(timeEnd)) || Date() > endDay
    }

    /**
     * On item task swipe
     */
    private fun onItemSwipe(list: MutableList<Task>, recyclerView: RecyclerView) {
        val leftCallback = GestureManager.SwipeCallbackLeft {
//            viewModel.deleteCategory(list[it])
//            if (list[it].taskState) {
//                list.remove(list[it])
//                listDone = list
//            } else {
//                list.remove(list[it])
//                listTodo = list
//            }
        }
        val rightCallback = GestureManager.SwipeCallbackRight {
//            viewModel.deleteCategory(list[it])
//            if (list[it].taskState) {
//                list.remove(list[it])
//                listDone = list
//            } else {
//                list.remove(list[it])
//                listTodo = list
//            }
        }


        val gestureManager = GestureManager(rightCallback, leftCallback)
        gestureManager.setBackgroundColorLeft(ColorDrawable(resourceColor(R.color.light_green)))
        gestureManager.setBackgroundColorRight(ColorDrawable(resourceColor(R.color.awesome)))

        gestureManager.setIconLeft(
            ContextCompat.getDrawable(
                this, R.drawable.ic_delete
            )
        )
        gestureManager.setTextLeft("Xoá")

        gestureManager.setIconRight(
            ContextCompat.getDrawable(
                this, R.drawable.ic_circle_checked
            )
        )
        gestureManager.setTextRight("Hoàn thành")

        gestureManager.setIconColor(resourceColor(R.color.white), resourceColor(R.color.white))
        gestureManager.setTextColor(resourceColor(R.color.white), resourceColor(R.color.white))

        val itemTouchHelper = ItemTouchHelper(gestureManager)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    /**
     * On task click
     */
    private fun onTaskClick(task: Task) {
        val intent = Intent(this, AddTaskActivity::class.java)
        intent.putExtra("TODO", task)
        startActivity(intent)
    }

    private fun setSelected(isSelect: IsSelect) {
        binding.run {
            when (isSelect) {
                IsSelect.TvAll -> {
                    tvAll.run {
                        isSelected = true
                        setTextColor(resourceColor(R.color.white))
                    }
                    tvTodo.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                    tvDone.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                    tvExpire.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                }

                IsSelect.TvTodo -> {
                    tvAll.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                    tvTodo.run {
                        isSelected = true
                        setTextColor(resourceColor(R.color.white))
                    }
                    tvDone.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                    tvExpire.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                }

                IsSelect.TvDone -> {
                    tvAll.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                    tvTodo.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                    tvDone.run {
                        isSelected = true
                        setTextColor(resourceColor(R.color.white))
                    }
                    tvExpire.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                }

                IsSelect.TvExpire -> {
                    tvAll.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                    tvTodo.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                    tvDone.run {
                        isSelected = false
                        setTextColor(resourceColor(R.color.arsenic))
                    }
                    tvExpire.run {
                        isSelected = true
                        setTextColor(resourceColor(R.color.white))
                    }
                }
            }
        }
    }

    enum class IsSelect {
        TvAll, TvTodo, TvDone, TvExpire
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityListAllTaskBinding =
        ActivityListAllTaskBinding.inflate(inflater)
}