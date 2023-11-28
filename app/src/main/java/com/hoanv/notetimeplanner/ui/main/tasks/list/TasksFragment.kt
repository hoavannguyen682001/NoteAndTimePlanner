package com.hoanv.notetimeplanner.ui.main.tasks.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.databinding.FragmentTasksBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.main.tasks.category.CategoryActivity
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment : BaseFragment<FragmentTasksBinding, TasksViewModel>() {
    override val viewModel: TasksViewModel by viewModels()

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTasksBinding = FragmentTasksBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {
        initView()
        intiListener()
        bindViewModel()
    }

    private fun initView() {
        binding.run {

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
}