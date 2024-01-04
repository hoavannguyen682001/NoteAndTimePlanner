package com.hoanv.notetimeplanner.ui.main.tasks.list

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.databinding.FragmentTasksBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.main.tasks.create.AddTaskActivity
import com.hoanv.notetimeplanner.ui.main.tasks.list.adapter.DoneTaskAdapter
import com.hoanv.notetimeplanner.ui.main.tasks.list.adapter.TaskAdapter
import com.hoanv.notetimeplanner.ui.main.tasks.list.adapter.TaskCategoryAdapter
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectInViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import swipe.gestures.GestureManager

@AndroidEntryPoint
class TasksFragment : BaseFragment<FragmentTasksBinding, TasksViewModel>() {
    override val viewModel: TasksViewModel by viewModels()

    private val categoryAdapter by lazy {
        TaskCategoryAdapter(requireContext()) { category, position ->
            selectedS.tryEmit(position)
            if (position == 0) {
                viewModel.getListTask()
            } else {
                viewModel.getListTaskByCategory(category)
            }
        }
    }

    private val taskAdapter by lazy {
        TaskAdapter(requireContext(), ::onTaskClick, ::onClickIconChecked)
    }

    private val doneTaskAdapter by lazy {
        DoneTaskAdapter(requireContext(), ::onTaskClick, ::onClickIconChecked)
    }

    private var selectedS = MutableStateFlow(0)

    private var mListTaskS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
    private var listTodo = listOf<Task>()
        set(value) {
            field = value
            mListTaskS.tryEmit(value)
        }

    private var mListDoneS = MutableSharedFlow<List<Task>>(extraBufferCapacity = 64)
    private var listDone = listOf<Task>()
        set(value) {
            field = value
            mListDoneS.tryEmit(value)
        }

    override fun setupViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentTasksBinding = FragmentTasksBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {
        initView()
        intiListener()
        bindViewModel()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getListCategory()
        viewModel.getListTask()
    }

    private fun initView() {
        binding.run {
//            rvCategory.run {
//                layoutManager =
//                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//                adapter = categoryAdapter
//                itemAnimator = null
//            }
//
            rvListTask.run {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = taskAdapter
            }
//
//            rvDoneTask.run {
//                layoutManager =
//                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//                adapter = doneTaskAdapter
//            }
        }
    }

    private fun intiListener() {
        binding.run {
//            ivOptionMenu.setOnSingleClickListener {
//                handleOptionMenu()
//            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                selectedS.combine(listCategory.asFlow()) { selected, list ->
                    Pair(selected, list)
                }.collectInViewLifecycle(this@TasksFragment) { item ->
                    val (select, state) = item

                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            val listCate = state.data.toMutableList()
                            listCate.add(0, Category(title = "Tất cả"))

                            listCate.mapIndexed { index, category ->
                                category.isSelected = index == select
                            }
                            categoryAdapter.submitList(listCate.map { it.ownCopy() })
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
//                            pbLoading.visible()
//                            nsvListTask.invisible()
                        }

                        is ResponseState.Success -> {
                            val task = mutableListOf<Task>()
                            val done = mutableListOf<Task>()

                            state.data.forEach {
                                if (it.taskState) {
                                    done.add(it)
                                } else {
                                    task.add(it)
                                }
                            }
                            listTodo = task
                            listDone = done

                            onItemSwipe(listTodo.toMutableList(), rvListTask)
//                            onItemSwipe(listDone.toMutableList(), rvDoneTask)

                            lifecycleScope.launch {
                                delay(500)
//                                pbLoading.gone()
//                                nsvListTask.visible()
//                                ivOptionMenu.visible()
                            }
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }
            }

            mListTaskS.collectInViewLifecycle(this@TasksFragment) { list ->
                taskAdapter.submitList(list)
            }

            mListDoneS.collectInViewLifecycle(this@TasksFragment) { list ->
                doneTaskAdapter.submitList(list)
            }
        }
    }

    /**
     * Handle option menu
     */
//    private fun handleOptionMenu() {
//        val popupMenu = PopupMenu(requireContext(), binding.ivOptionMenu)
//        popupMenu.inflate(R.menu.top_menu)
//
//        popupMenu.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.itemCategory -> {
//                    startActivity(Intent(requireContext(), CategoryActivity::class.java))
//                }
//
//                R.id.itemSearch -> {
//                    toastSuccess("searchhhhhhhhhhhhhhhhhhhhhhhhhhhhh")
//                }
//            }
//            false
//        }
//
//        popupMenu.show()
//    }

    /**
     * On item task swipe
     */
    private fun onItemSwipe(list: MutableList<Task>, recyclerView: RecyclerView) {
        val leftCallback = GestureManager.SwipeCallbackLeft {
            viewModel.deleteCategory(list[it])
            if (list[it].taskState) {
                list.remove(list[it])
                listDone = list
            } else {
                list.remove(list[it])
                listTodo = list
            }
        }

        val gestureManager = GestureManager(leftCallback)
        gestureManager.setBackgroundColorLeft(ColorDrawable(Color.GREEN))
        gestureManager.setIconLeft(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_delete
            )
        )
        ItemTouchHelper(gestureManager).attachToRecyclerView(recyclerView)
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
    }}
