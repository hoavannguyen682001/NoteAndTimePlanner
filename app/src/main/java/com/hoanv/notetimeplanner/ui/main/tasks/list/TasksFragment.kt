package com.hoanv.notetimeplanner.ui.main.tasks.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Todo
import com.hoanv.notetimeplanner.databinding.FragmentTasksBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.main.tasks.category.CategoryActivity
import com.hoanv.notetimeplanner.ui.main.tasks.create.AddTaskActivity
import com.hoanv.notetimeplanner.ui.main.tasks.list.adapter.TaskAdapter
import com.hoanv.notetimeplanner.ui.main.tasks.list.adapter.TaskCategoryAdapter
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectInViewLifecycle
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow

@AndroidEntryPoint
class TasksFragment : BaseFragment<FragmentTasksBinding, TasksViewModel>() {
    override val viewModel: TasksViewModel by viewModels()

    private val categoryAdapter by lazy {
        TaskCategoryAdapter(requireContext(), ::onItemCategoryClick)
    }

    private val taskAdapter by lazy {
        TaskAdapter(requireContext(), ::onTaskClick, ::onClickIconChecked)
    }

    private var mListCategoryS = MutableSharedFlow<List<Category>>(extraBufferCapacity = 64)
    private var listCategoryS = listOf<Category>()
        set(value) {
            field = value
            mListCategoryS.tryEmit(value)
        }

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
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
            rvCategory.run {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = categoryAdapter
            }

            rvTodoTask.run {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = taskAdapter
            }
        }
    }

    private fun intiListener() {
        binding.run {
            ivOptionMenu.setOnSingleClickListener {
                handleOptionMenu()
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                listCategory.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            listCategoryS = state.data
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listTask.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            taskAdapter.submitList(state.data)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }
            }

            mListCategoryS.collectInViewLifecycle(this@TasksFragment) { list ->
                categoryAdapter.submitList(list.map { it.ownCopy() }) {
                    Log.d("###", "${list}")
                }
            }
        }
    }

    private fun handleOptionMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.ivOptionMenu)
        popupMenu.inflate(R.menu.top_menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemCategory -> {
                    startActivity(Intent(requireContext(), CategoryActivity::class.java))
                }

                R.id.itemSearch -> {
                    toastSuccess("searchhhhhhhhhhhhhhhhhhhhhhhhhhhhh")
                }
            }
            false
        }

        popupMenu.show()
    }

    private fun onItemCategoryClick(category: Category, position: Int) {
        val newList = listCategoryS.map {
            it.isSelected = it.id == category.id
            it
        }
        listCategoryS = newList
    }

    private fun onTaskClick(todo: Todo) {
        val intent = Intent(requireActivity(), AddTaskActivity::class.java)
        intent.putExtra("TODO", todo)
        startActivity(intent)
    }

    private fun onClickIconChecked(todo: Todo) {
        viewModel.deleteCategory(todo)
        viewModel.getListTask()
    }
}