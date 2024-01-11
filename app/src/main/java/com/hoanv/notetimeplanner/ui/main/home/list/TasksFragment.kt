package com.hoanv.notetimeplanner.ui.main.home.list

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.TypeTask
import com.hoanv.notetimeplanner.databinding.FragmentTasksBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.evenbus.CheckReloadListTask
import com.hoanv.notetimeplanner.ui.evenbus.ReloadUserInfo
import com.hoanv.notetimeplanner.ui.evenbus.UserInfoEvent
import com.hoanv.notetimeplanner.ui.main.home.create.AddTaskActivity
import com.hoanv.notetimeplanner.ui.main.home.list.adapter.TaskAdapter
import com.hoanv.notetimeplanner.ui.main.listTask.ListAllTaskActivity
import com.hoanv.notetimeplanner.utils.AppConstant.TASK_TYPE
import com.hoanv.notetimeplanner.utils.ResponseState
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class TasksFragment : BaseFragment<FragmentTasksBinding, TasksViewModel>() {
    override val viewModel: TasksViewModel by viewModels()

//    private val categoryAdapter by lazy {
//        TaskCategoryAdapter(requireContext()) { category, position ->
//            selectedS.tryEmit(position)
//            if (position == 0) {
//                viewModel.getListTask()
//            } else {
//                viewModel.getListTaskByCategory(category)
//            }
//        }
//    }

    private val taskAdapter by lazy {
        TaskAdapter(requireContext(), ::onTaskClick, ::onClickIconChecked)
    }

//    private val doneTaskAdapter by lazy {
//        DoneTaskAdapter(requireContext(), ::onTaskClick, ::onClickIconChecked)
//    }

//    private var selectedS = MutableStateFlow(0)

    private var mListTaskS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
    private var _listTask = listOf<Task>()
        set(value) {
            field = value
            mListTaskS.tryEmit(value)
        }

//    private var mListDoneS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
//    private var listDone = listOf<Task>()
//        set(value) {
//            field = value
//            mListDoneS.tryEmit(value)
//        }

    override fun setupViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentTasksBinding = FragmentTasksBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {
        initView()
        intiListener()
        bindViewModel()
        EventBus.getDefault().register(this)
    }

    private fun initView() {
        binding.run {
            rvListTask.run {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = taskAdapter
            }
        }
    }

    private fun intiListener() {
        binding.run {
            tvSeeAll.setOnSingleClickListener {
                startActivity(Intent(requireContext(), ListAllTaskActivity::class.java).apply {
                    putExtra(TASK_TYPE, "Personal")
                })
            }

            tvSeeAllPr.setOnSingleClickListener {
                startActivity(Intent(requireContext(), ListAllTaskActivity::class.java).apply {
                    putExtra(TASK_TYPE, "Group")
                })
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                userInfo.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        ResponseState.Start -> {}

                        is ResponseState.Success -> {/* Event bus user info */
                            EventBus.getDefault().post(UserInfoEvent(state.data))

                            tvUserName.text = getString(R.string.hello_user, state.data.userName)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listTask.asFlow().collectInViewLifecycle(this@TasksFragment) { state ->
                    when (state) {
                        ResponseState.Start -> {
                            lottieAnim.visible()
                            rvListTask.gone()
                        }

                        is ResponseState.Success -> {
                            val task = mutableListOf<Task>()
                            val group = mutableListOf<Task>()

                            state.data.forEach {
                                if (it.typeTask == TypeTask.PERSONAL) {
                                    task.add(it)
                                } else {
                                    group.add(it)
                                }
                            }

                            _listTask = task
                            setGroupTask(group)

                            onItemSwipe(_listTask.toMutableList(), rvListTask)
                        }

                        is ResponseState.Failure -> {
                            lottieAnim.gone()
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }
            }

            mListTaskS.collectInViewLifecycle(this@TasksFragment) { list ->
                taskAdapter.submitList(list) {
                    lifecycleScope.launch {
                        delay(1000)
                        lottieAnim.gone()
                        rvListTask.visible()
                    }
                }
            }

//            mListDoneS.collectInViewLifecycle(this@TasksFragment) { list ->
//                doneTaskAdapter.submitList(list)
//            }
        }
    }

    private fun setGroupTask(listTask: List<Task>) {
        val alpha = 191 //alpha: 75%
        val taskOne = listTask[0]
        val taskTrue = listTask[1]
        binding.run {
            taskOne.run {
                tvTaskGroupName.text = title
                tvDate.text = getString(
                    R.string.start_to_end_group_task,
                    regexDayMonth(startDay!!),
                    regexDayMonth(endDay!!)
                )
                tvTaskNumb.text = getString(R.string.number_of_group_tasks, subTask.size)
                tvMember.text = getString(R.string.member_number, member.size)

                Glide.with(requireContext()).load(category.icon.iconUrl).into(ivCategory)

                ivCategory.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(category.icon.iconColor))

                cslTaskLeft.backgroundTintList = ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(
                        Color.parseColor(category.icon.iconColor), alpha
                    )
                )
            }

            taskTrue.run {
                tvTaskGroupNameR.text = title
                tvDateR.text = getString(
                    R.string.start_to_end_group_task,
                    regexDayMonth(startDay!!),
                    regexDayMonth(endDay!!)
                )
                tvTaskNumbR.text = getString(R.string.number_of_group_tasks, subTask.size)
                tvMemberR.text = getString(R.string.member_number, member.size)

                Glide.with(requireContext()).load(category.icon.iconUrl).into(ivCategoryR)

                ivCategoryR.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(category.icon.iconColor))

                cslTaskRight.backgroundTintList = ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(
                        Color.parseColor(category.icon.iconColor), alpha
                    )
                )
            }
        }
    }

    private fun regexDayMonth(date: String): String {

        // Sử dụng regex để lấy chuỗi "29-01"
        val result = date.replace(Regex("(\\d{2}-\\d{2}).*"), "$1")
        return result
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
                requireContext(), R.drawable.ic_delete
            )
        )
        gestureManager.setTextLeft("Xoá")
        gestureManager.setIconRight(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_circle_checked
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
        val intent = Intent(requireActivity(), AddTaskActivity::class.java)
        intent.putExtra("TODO", task)
        startActivity(intent)
    }

    /**
     * On icon check click
     */
    private fun onClickIconChecked(task: Task) {
//        val tempListTodo = mutableListOf<Task>()
//        val tempListDone = mutableListOf<Task>()
//
//        tempListTodo.addAll(listTodo)
//        tempListDone.addAll(listDone)
//
//        if (task.taskState) {
//            tempListTodo.add(task)
//            tempListDone.remove(task)
//        } else {
//            tempListTodo.remove(task)
//            tempListDone.add(task)
//        }
//
//        task.taskState = !task.taskState
//        viewModel.updateTask(task)
//
//        Log.d("Todoooo", "$tempListTodo")
//        Log.d("Todoooo done", "$tempListDone")
//
//        listTodo = tempListTodo
//        listDone = tempListDone
    }

    @Subscribe
    fun reloadListTask(checked: CheckReloadListTask) {
        if (checked.isReload) {
            viewModel.getListTask()
        }
    }

    @Subscribe
    fun reloadUserInfo(checked: ReloadUserInfo) {
        if (checked.isReload) {
            viewModel.getUserInfo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
