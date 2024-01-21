package com.hoanv.notetimeplanner.ui.main.calendar

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.databinding.FragmentCalendarBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.main.home.create.AddTaskActivity
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectIn
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.visible
import com.hoanv.notetimeplanner.utils.widget.swipe.GestureManager
import dagger.hilt.android.AndroidEntryPoint
import fxc.dev.common.extension.resourceColor
import kotlinx.coroutines.flow.MutableSharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CalendarFragment : BaseFragment<FragmentCalendarBinding, CalendarViewModel>() {
    override val viewModel: CalendarViewModel by viewModels()

    private val _listTaskS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
    private var filterListTask = mutableListOf<Task>()
    private val mListTaskS = mutableListOf<Task>()
    private var listTaskS = listOf<Task>()
        set(value) {
            field = value
            _listTaskS.tryEmit(value)
        }

    private val _listGroupTaskS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
    private var filterListGroupTask = mutableListOf<Task>()
    private val mListGroupTaskS = mutableListOf<Task>()
    private var listGroupTaskS = listOf<Task>()
        set(value) {
            field = value
            _listGroupTaskS.tryEmit(value)
        }

    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val date = Date()
    private var currentDay = formatter.format(date)

    private val calendarAdapter by lazy {
        CalendarTaskAdapter(requireContext()) {
            val intent = Intent(requireContext(), AddTaskActivity::class.java)
            intent.putExtra("TODO", it)
            startActivity(intent)
        }
    }

    private val calendarGroupAdapter by lazy {
        CalendarGroupTaskAdapter(requireContext(), {
            val intent = Intent(requireContext(), AddTaskActivity::class.java)
            intent.putExtra("TODO", it)
            startActivity(intent)
        }, { task, view ->
            handleOptionMenu(task, view)
        })
    }

    private var datePicker: String = ""

    override fun setupViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentCalendarBinding = FragmentCalendarBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {
        val vietnameseLocale = Locale("vi", "VN")
        val resources: Resources = resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(vietnameseLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        initView()
        initListener()
        bindViewModel()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getListTask()
        binding.run {
            tvTaskPersonal.isSelected = true
            tvTaskGroup.isSelected = false
        }
    }

    private fun initView() {
        binding.run {
            tvTaskPersonal.isSelected = true
            datePicker = currentDay
            rvTask.run {
                adapter = calendarAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }

            rvListTaskGroup.run {
                rvListTaskGroup.run {
                    layoutManager = GridLayoutManager(requireContext(), 2)
                    adapter = calendarGroupAdapter
                }
            }
        }
    }

    private fun initListener() {
        binding.run {
            calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
                val date = getString(
                    R.string.date_selected,
                    if (dayOfMonth < 10) "0${dayOfMonth}" else "$dayOfMonth",
                    if (month < 9) "0${month + 1}" else "${month + 1}",
                    "$year"
                )
                datePicker = date
                if (tvTaskPersonal.isSelected) {
                    filterByDay(date)
                }
                if (tvTaskGroup.isSelected) {
                    filterByDayGroupTask(date)
                }
            }

            tvTaskPersonal.setOnClickListener {
                tvTaskPersonal.run {
                    isSelected = true
                    tvTaskPersonal.setTextColor(resourceColor(R.color.white))
                }
                tvTaskGroup.run {
                    isSelected = false
                    setTextColor(resourceColor(R.color.arsenic))
                }

                rvListTaskGroup.gone()
                filterByDay(datePicker)
            }

            tvTaskGroup.setOnClickListener {
                tvTaskGroup.run {
                    isSelected = true
                    setTextColor(resourceColor(R.color.white))
                }
                tvTaskPersonal.run {
                    isSelected = false
                    setTextColor(resourceColor(R.color.arsenic))
                }

                rvTask.gone()
                filterByDayGroupTask(datePicker)
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                listTaskPersonal.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            /* List personal task */
//                            listTaskS = state.data
                            mListTaskS.clear()
                            mListTaskS.addAll(state.data)
                            filterByDay(currentDay)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listGroupTask.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            /* List group task */
//                            listGroupTaskS = state.data
                            mListGroupTaskS.clear()
                            mListGroupTaskS.addAll(state.data)
                            filterByDayGroupTask(currentDay)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }
            }
            _listTaskS.collectIn(viewLifecycleOwner) { list ->
                if (list.isNotEmpty()) {
                    calendarAdapter.submitList(list.map { it.copy() }) { onItemSwipe(rvTask) }
                    rvTask.adapter?.notifyDataSetChanged()
                    rvTask.visible()
                    tvEmpty.gone()
                    ivEmpty.gone()
                } else {
                    rvTask.gone()
                    tvEmpty.visible()
                    ivEmpty.visible()
                }
            }

            _listGroupTaskS.collectIn(viewLifecycleOwner) { list ->
                if (list.isNotEmpty()) {
                    calendarGroupAdapter.submitList(list.map { it.copy() })
                    if (tvTaskGroup.isSelected) {
                        rvListTaskGroup.visible()
                    }
                    rvListTaskGroup.adapter?.notifyDataSetChanged()

                    tvEmpty.gone()
                    ivEmpty.gone()
                } else {
                    rvListTaskGroup.gone()
                    tvEmpty.visible()
                    ivEmpty.visible()
                }
            }
        }
    }

    private fun filterByDay(date: String) {
        val tempList = mListTaskS.filter {
            it.startDay == date || it.endDay == date
        }
        filterListTask.clear()
        filterListTask.addAll(tempList)
        listTaskS = tempList
    }

    private fun filterByDayGroupTask(date: String) {
        val tempListS = mListGroupTaskS.filter {
            it.startDay == date || it.endDay == date
        }
        filterListGroupTask.clear()
        filterListGroupTask.addAll(tempListS)
        listGroupTaskS = tempListS
    }

    /**
     * On item task swipe
     */
    private fun onItemSwipe(recyclerView: RecyclerView) {
        val leftCallback = GestureManager.SwipeCallbackLeft {
            viewModel.deleteTask(filterListTask[it])

            filterListTask.removeAt(it)
            mListTaskS.removeAt(it)
            listTaskS = filterListTask
        }
        val rightCallback = GestureManager.SwipeCallbackRight { index ->
            val templist = filterListTask.map {
                filterListTask[index].taskState = true
                it
            }
            viewModel.updateTask(filterListTask[index])

            listTaskS = templist
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

    private fun handleOptionMenu(task: Task, view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.edit_group_task)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemDone -> {
                    val tempList = filterListGroupTask.map { item ->
                        if (item.id == task.id) {
                            item.taskState = true
                            task.taskState = true
                        }
                        item
                    }
                    listGroupTaskS = tempList
                    viewModel.updateTask(task)
                }

                R.id.itemDel -> {
                    viewModel.deleteTask(task)
                    mListGroupTaskS.remove(task)
                    filterListGroupTask.remove(task)
                    listGroupTaskS = filterListGroupTask
                    Log.d("listGroupTaskS", "${listGroupTaskS.size}")
                }
            }
            false
        }

        popupMenu.show()
    }
}