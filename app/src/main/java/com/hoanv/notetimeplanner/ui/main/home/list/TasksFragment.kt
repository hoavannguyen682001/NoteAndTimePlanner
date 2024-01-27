package com.hoanv.notetimeplanner.ui.main.home.list

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.TypeTask
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.databinding.FragmentTasksBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.evenbus.CheckReloadListTask
import com.hoanv.notetimeplanner.ui.evenbus.ReloadUserInfo
import com.hoanv.notetimeplanner.ui.evenbus.UserInfoEvent
import com.hoanv.notetimeplanner.ui.main.home.create.AddTaskActivity
import com.hoanv.notetimeplanner.ui.main.home.list.adapter.TaskAdapter
import com.hoanv.notetimeplanner.ui.main.listTask.ListAllTaskActivity
import com.hoanv.notetimeplanner.utils.AppConstant.TASK_TYPE
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectInViewLifecycle
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import com.hoanv.notetimeplanner.utils.extension.visible
import com.hoanv.notetimeplanner.utils.widget.swipe.GestureManager
import dagger.hilt.android.AndroidEntryPoint
import fxc.dev.common.extension.resourceColor
import kotlinx.coroutines.flow.MutableSharedFlow
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TasksFragment : BaseFragment<FragmentTasksBinding, TasksViewModel>() {
    override val viewModel: TasksViewModel by viewModels()

    private val taskAdapter by lazy {
        TaskAdapter(requireContext(), ::onTaskClick)
    }

    private var mListTaskS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
    private var listTaskS = mutableListOf<Task>()
    private var _listTask = listOf<Task>()
        set(value) {
            field = value
            mListTaskS.tryEmit(value)
        }

    private var groupTaskLeft: Task? = null
    private var groupTaskRight: Task? = null

    private var userInfoS = UserInfo()

    override fun setupViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentTasksBinding = FragmentTasksBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {
        initView()
        intiListener()
        bindViewModel()
        EventBus.getDefault().register(this)
    }

    override fun onStart() {
        super.onStart()
        if (Pref.isLoading) {
            viewModel.getListTask()
        }
        Pref.isLoading = true
    }

    private fun initView() {
        binding.run {
            rvListTask.run {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = taskAdapter
                itemAnimator = null
            }
        }
    }

    private fun intiListener() {
        binding.run {
            tvSeeAll.setOnSingleClickListener {
                startActivity(Intent(requireContext(), ListAllTaskActivity::class.java).apply {
                    putExtra(TASK_TYPE, TypeTask.PERSONAL.name)
                })
            }

            tvSeeAllPr.setOnSingleClickListener {
                startActivity(Intent(requireContext(), ListAllTaskActivity::class.java).apply {
                    putExtra(TASK_TYPE, TypeTask.GROUP.name)
                })
            }

            cslGroupTaskLeft.setOnSingleClickListener {
                val intent = Intent(requireActivity(), AddTaskActivity::class.java)
                intent.putExtra("TODO", groupTaskLeft)
                startActivity(intent)
            }

            cslGroupTaskRight.setOnSingleClickListener {
                val intent = Intent(requireActivity(), AddTaskActivity::class.java)
                intent.putExtra("TODO", groupTaskRight)
                startActivity(intent)
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
                            userInfoS = state.data
                            tvUserName.text = getString(R.string.hello_user, state.data.userName)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listTaskPersonal.observe(this@TasksFragment) { state ->
                    when (state) {
                        ResponseState.Start -> {
                            lottieAnim.visible()
                            rvListTask.gone()
                        }

                        is ResponseState.Success -> {
                            if (state.data.isNotEmpty()) {
                                listTaskS.clear()
                                state.data.forEach {
                                    if (!expireDay(it) && !it.taskState) {
                                        listTaskS.add(it)
                                    }
                                }
                                _listTask = listTaskS
                                onItemSwipe(rvListTask)
                            }
                        }

                        is ResponseState.Failure -> {
                            lottieAnim.gone()
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listGroupTask.observe(this@TasksFragment) { state ->
                    when (state) {
                        ResponseState.Start -> {
                            cslGroupTaskLeft.gone()
                            cslGroupTaskRight.gone()
                        }

                        is ResponseState.Success -> {
                            if (state.data.isNotEmpty()) {
                                setGroupTask(state.data.sortedByDescending {
                                    it.createdAt.toLong()
                                })
                                cslGroupTaskLeft.visible()
                                cslGroupTaskRight.visible()
                            } else {
                                cslGroupTaskLeft.gone()
                                cslGroupTaskRight.gone()
                            }
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
                if (list.isNotEmpty()) {
                    taskAdapter.submitList(list.map { it.copy() }) {
                        lottieAnim.gone()
                        rvListTask.visible()
                    }
                    rvListTask.adapter?.notifyDataSetChanged()
                    tvEmpty.gone()
                    ivEmpty.gone()
                } else {
                    lottieAnim.gone()
                    rvListTask.gone()
                    tvEmpty.visible()
                    ivEmpty.visible()
                }
            }
        }
    }

    private fun setGroupTask(listTask: List<Task>) {
        val alpha = 191 //alpha: 75%
        val taskOne = listTask[0]
        groupTaskLeft = taskOne
        val taskTwo = listTask[1]
        groupTaskRight = taskTwo
        binding.run {
            taskOne.run {
                tvTaskGroupName.text = title
                tvDate.text = getString(
                    R.string.start_to_end_group_task,
                    regexDayMonth(startDay!!),
                    regexDayMonth(endDay!!)
                )
                tvTaskNumb.text = getString(R.string.number_of_group_tasks, subTask.size)
                tvMember.text = getString(R.string.member_number, listMember.size)

                Glide.with(requireContext()).load(category.icon.iconUrl).into(ivCategory)

                ivCategory.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(category.icon.iconColor))

                cslTaskLeft.backgroundTintList = ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(
                        Color.parseColor(category.icon.iconColor), alpha
                    )
                )
                ivNext.setColorFilter(
                    Color.parseColor(category.icon.iconColor),
                    PorterDuff.Mode.SRC_IN
                )

                binding.run {
                    if (taskOne.taskState) {
                        tvTaskGroupName.paintFlags =
                            tvTaskGroupName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        tvStateTask.text = requireContext().getString(R.string.text_done)
                        tvStateTask.backgroundTintList =
                            requireContext().getColorStateList(R.color.light_green)
                    } else {
                        binding.tvTaskGroupName.paintFlags =
                            tvTaskGroupName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        if (expireDay(taskOne)) {
                            tvStateTask.text = requireContext().getString(R.string.text_expire)
                            tvStateTask.backgroundTintList =
                                requireContext().getColorStateList(R.color.orange_crayola)
                        } else {
                            tvStateTask.text = requireContext().getString(R.string.text_todo)
                            tvStateTask.backgroundTintList =
                                requireContext().getColorStateList(R.color.tufts_blue)
                        }
                    }
                }
            }

            taskTwo.run {
                tvTaskGroupNameR.text = title
                tvDateR.text = getString(
                    R.string.start_to_end_group_task,
                    regexDayMonth(startDay!!),
                    regexDayMonth(endDay!!)
                )
                tvTaskNumbR.text = getString(R.string.number_of_group_tasks, subTask.size)
                tvMemberR.text = getString(R.string.member_number, listMember.size)

                Glide.with(requireContext()).load(category.icon.iconUrl).into(ivCategoryR)

                ivCategoryR.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(category.icon.iconColor))

                cslTaskRight.backgroundTintList = ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(
                        Color.parseColor(category.icon.iconColor), alpha
                    )
                )
                ivNextR.setColorFilter(
                    Color.parseColor(category.icon.iconColor),
                    PorterDuff.Mode.SRC_IN
                )

                binding.run {
                    if (taskTwo.taskState) {
                        tvTaskGroupNameR.paintFlags =
                            tvTaskGroupName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        tvStateTaskR.text = requireContext().getString(R.string.text_done)
                        tvStateTaskR.backgroundTintList =
                            requireContext().getColorStateList(R.color.light_green)
                    } else {
                        binding.tvTaskGroupNameR.paintFlags =
                            tvTaskGroupNameR.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        if (expireDay(taskTwo)) {
                            tvStateTaskR.text = requireContext().getString(R.string.text_expire)
                            tvStateTaskR.backgroundTintList =
                                requireContext().getColorStateList(R.color.orange_crayola)
                        } else {
                            tvStateTaskR.text = requireContext().getString(R.string.text_todo)
                            tvStateTaskR.backgroundTintList =
                                requireContext().getColorStateList(R.color.tufts_blue)
                        }
                    }
                }
            }
        }
    }

    private fun expireDay(task: Task): Boolean {
        val expire = "${task.endDay} ${task.timeEnd}"
        val endDay = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).parse(
            expire
        )

        val end = Calendar.getInstance().apply { time = endDay!! }
        val now = Calendar.getInstance().apply { time = Date() }

        end.set(Calendar.SECOND, 59)
        now.set(Calendar.SECOND, 0)

        val endTime = end.timeInMillis
        val timeNow = now.timeInMillis
        /* Check if end day before today */
        return timeNow > endTime
    }

    private fun regexDayMonth(date: String): String {
        // Use regex to get string "dd-MM"
        val result = date.replace(Regex("(\\d{2}-\\d{2}).*"), "$1")
        return result
    }

    /**
     * On item task swipe
     */
    private fun onItemSwipe(recyclerView: RecyclerView) {
        val leftCallback = GestureManager.SwipeCallbackLeft {
            viewModel.deleteTask(listTaskS[it])

            listTaskS.removeAt(it)
            _listTask = listTaskS
        }
        val rightCallback = GestureManager.SwipeCallbackRight { index ->
            listTaskS[index].taskState = true
            viewModel.updateTask(listTaskS[index])

            listTaskS.removeAt(index)
            _listTask = listTaskS
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
