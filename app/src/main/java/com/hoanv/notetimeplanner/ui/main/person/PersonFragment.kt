package com.hoanv.notetimeplanner.ui.main.person

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.databinding.FragmentPersonBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.evenbus.UserInfoEvent
import com.hoanv.notetimeplanner.ui.main.person.activity.EditProfileActivity
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import fxc.dev.common.extension.resourceColor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@AndroidEntryPoint
class PersonFragment : BaseFragment<FragmentPersonBinding, PersonViewModel>() {
    override val viewModel: PersonViewModel by viewModels()

    private val listCategoriesS = mutableListOf<Category>()
    private var user = UserInfo()
    override fun setupViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentPersonBinding = FragmentPersonBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
        EventBus.getDefault().register(this)
    }

    private fun initView() {
    }

    private fun initListener() {
        binding.run {
            ivProfile.setOnSingleClickListener {
                startActivity(
                    Intent(requireActivity(), EditProfileActivity::class.java).apply {
                        putExtra("USER", user)
                    }
                )
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                listCategories.observe(viewLifecycleOwner) {
                    listCategoriesS.addAll(it)
                }

                listTask.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        ResponseState.Start -> {

                        }

                        is ResponseState.Success -> {
                            val listDone = state.data.filter {
                                it.taskState
                            }
                            tvNumbOfSum.text = getString(R.string.numb_of_task, state.data.size)
                            tvNumbOfDone.text = getString(R.string.numb_of_task, listDone.size)
                            progressChart(state.data)
                            setupPieChart(state.data)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }
            }
        }
    }

    private fun progressChart(list: List<Task>) {
        binding.run {
            pbTaskDone.max = list.size
            pbTaskTodo.max = list.size
            pbExpire.max = list.size

            var taskDone = 0
            var taskTodo = 0
            var taskExpire = 0

            list.forEach {
                if (it.taskState) {
                    taskDone++
                } else {
                    if (expireDay(it)) {
                        taskExpire++
                    } else {
                        taskTodo++
                    }
                }
            }

            tvProgressDone.text = taskDone.toString()
            tvProgressExpire.text = taskExpire.toString()
            tvProgressTodo.text = taskTodo.toString()

            pbTaskDone.progress = taskDone
            pbTaskTodo.progress = taskTodo
            pbExpire.progress = taskExpire
        }
    }

    private fun setupPieChart(listTask: List<Task>) {
        val colors = mutableListOf(
            resourceColor(R.color.colorPrimary),
            resourceColor(R.color.picton_blue),
        )

        binding.run {
            val list = ArrayList<PieEntry>()
            list.add(PieEntry(listTask.filter { it.taskState }.size.toFloat(), ""))
            list.add(PieEntry(listTask.filter { !it.taskState }.size.toFloat(), ""))

            val pieDataSet = PieDataSet(list, "")

            pieDataSet.colors = colors
            pieDataSet.valueTextColor = Color.BLACK
            pieDataSet.valueTextSize = 12f
            pieDataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.roundToInt()}"
                }
            }

            pieChartStatistical.run {
                data = PieData(pieDataSet)
                legend.isEnabled = false
                setDrawEntryLabels(false)
                description.isEnabled = false
                animateY(1000)
                invalidate()
            }
        }
    }

    private fun expireDay(task: Task): Boolean {
        val expire = "${task.endDay} ${task.timeEnd}"
        val endDay =
            SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).parse(
                expire
            )
        /* Check if end day before today or if timeEnd before current time */
        return Date().after(endDay)
    }

    @Subscribe
    fun getUserInfo(event: UserInfoEvent) {
        user = event.userInfo
        binding.run {
            tvUserName.text = getString(R.string.hello_user, user.userName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}