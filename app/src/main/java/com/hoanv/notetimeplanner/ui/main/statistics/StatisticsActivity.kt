package com.hoanv.notetimeplanner.ui.main.statistics

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.TypeTask
import com.hoanv.notetimeplanner.databinding.ActivityStatisticsBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectIn
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.hilt.android.AndroidEntryPoint
import fxc.dev.common.extension.resourceColor
import kotlinx.coroutines.flow.MutableSharedFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@AndroidEntryPoint
class StatisticsActivity : BaseActivity<ActivityStatisticsBinding, StatisticsVM>() {
    override val viewModel: StatisticsVM by viewModels()

    private val _listTaskS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
    private val mListTaskS = mutableListOf<Task>()
    private var listTaskS = listOf<Task>()
        set(value) {
            field = value
            _listTaskS.tryEmit(value)
        }


    private val calendar = Calendar.getInstance()
    private val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)

    override fun init(savedInstanceState: Bundle?) {
        initListener()
        bindViewModel()
    }

    private fun initListener() {
        binding.run {
            collapsingCalendar.setOnClickListener {

            }
            collapsingCalendar.setCalendarListener(object : CollapsibleCalendar.CalendarListener {
                override fun onClickListener() {
                }

                override fun onDataUpdate() {
                }

                override fun onDayChanged() {
                }

                override fun onDaySelect() {
                }

                override fun onItemClick(v: View) {
                    Log.d("collapsingCalendar", "${collapsingCalendar.expanded}")
                }

                override fun onMonthChange() {
                    if (collapsingCalendar.expanded) {
                        filterTask(collapsingCalendar.month + 1, true)
                    }
                }

                override fun onWeekChange(position: Int) {
                    filterTask(position + 1, false)
                    Log.d("onWeekChange", "${position + 1}")
                }
            })
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                listTask.observe(this@StatisticsActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {

                        }

                        is ResponseState.Success -> {

                            mListTaskS.clear()
                            mListTaskS.addAll(state.data)
                            listTaskS = mListTaskS
                            filterTask(currentWeek, false)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }
            }
            _listTaskS.collectIn(this@StatisticsActivity) {
                progressChart(it)
                setupPieChart(it)
            }
        }
    }

    private fun filterTask(position: Int, isExpanded: Boolean) {
        val tempList = mutableListOf<Task>()

        if (isExpanded) {
            mListTaskS.forEach {
                if (isDateInMonth(position, it.startDay!!)
                    || isDateInMonth(position, it.endDay!!)
                ) {
                    tempList.add(it)
                }
            }
        } else {
            mListTaskS.forEach {
                if (isDateInCurrentWeek(position, it.startDay!!) || isDateInCurrentWeek(
                        position,
                        it.endDay!!
                    )
                ) {
                    tempList.add(it)
                }
            }
        }

        listTaskS = tempList
    }

    private fun isDateInCurrentWeek(week: Int, dateString: String): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateToCheck = Calendar.getInstance()
        dateToCheck.time = dateFormat.parse(dateString)!!
        val dateWeek = dateToCheck.get(Calendar.WEEK_OF_MONTH)

        Log.d("onWeekChange", "${dateString} - $dateWeek")


        return week == dateWeek
    }

    private fun isDateInMonth(month: Int, dateString: String): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateToCheck = Calendar.getInstance()
        dateToCheck.time = dateFormat.parse(dateString)!!
        val dateMonth = dateToCheck.get(Calendar.MONTH)

        Log.d("onMonthChange", "${month} - $dateMonth")


        return month == dateMonth
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
            list.add(
                PieEntry(
                    listTask.filter { it.typeTask == TypeTask.PERSONAL }.size.toFloat(),
                    ""
                )
            )
            list.add(PieEntry(listTask.filter { it.typeTask == TypeTask.GROUP }.size.toFloat(), ""))

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

    override fun setupViewBinding(inflater: LayoutInflater): ActivityStatisticsBinding =
        ActivityStatisticsBinding.inflate(inflater)
}