package com.hoanv.notetimeplanner.ui.main.tasks.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.databinding.ActivityCategoryBinding
import com.hoanv.notetimeplanner.databinding.DialogAddCategoryBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import com.hoanv.notetimeplanner.utils.extension.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryActivity : BaseActivity<ActivityCategoryBinding, CategoryVM>() {
    override val viewModel: CategoryVM by viewModels()

    private val categoryAdapter by lazy {
        CategoryAdapter(this, ::handleOptionMenu)
    }

    private val iconAdapter by lazy {
        IconCategoryAdapter(this) {}
    }

    private lateinit var dialogBinding: DialogAddCategoryBinding
    private lateinit var alertDialog: AlertDialog


    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.run {
            rvCategory.run {
                layoutManager = LinearLayoutManager(
                    this@CategoryActivity, LinearLayoutManager.VERTICAL, false
                )
                adapter = categoryAdapter
            }

            dialogBinding =
                DialogAddCategoryBinding.inflate(LayoutInflater.from(this@CategoryActivity))
            alertDialog =
                AlertDialog.Builder(this@CategoryActivity, R.style.AppCompat_AlertDialog)
                    .setView(dialogBinding.root)
                    .setCancelable(false).create()

            dialogBinding.run {
                rvIconCategory.run {
                    adapter = iconAdapter
                    layoutManager = LinearLayoutManager(
                        this@CategoryActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }
            }
        }
    }

    private fun initListener() {
        binding.run {
            tvAddCategory.setOnSingleClickListener {
                dialogAddCategory(null)
            }
            ivBack.setOnSingleClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                iconCategory.observe(this@CategoryActivity) {
                    iconAdapter.submitList(it)
                    Log.d("LISTTTTTTTTTTTTT", "$it")
                }

                addCategoryTriggerS.observe(this@CategoryActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {}

                        is ResponseState.Success -> {
                            toastSuccess(state.data)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listCategory.observe(this@CategoryActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                            pbLoading.visible()
                            tvAddCategory.gone()
                        }

                        is ResponseState.Success -> {
                            categoryAdapter.submitList(state.data) {
                                Log.d("###", "${state.data}")
                                tvAddCategory.visible()
                                pbLoading.gone()
                            }
                            toastSuccess("${state.data}")
                        }

                        is ResponseState.Failure -> {
                            pbLoading.gone()
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                updateCategoryTriggerS.observe(this@CategoryActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {}

                        is ResponseState.Success -> {
                            toastSuccess(state.data)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
                }

                deleteCategoryTriggerS.observe(this@CategoryActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {}

                        is ResponseState.Success -> {
                            toastSuccess(state.data)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                        }
                    }
                }
            }
        }
    }

    private fun handleOptionMenu(category: Category, view: View) {
        val popupMenu = PopupMenu(this@CategoryActivity, view)
        popupMenu.inflate(R.menu.category_menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemUpdate -> {
                    dialogAddCategory(category)
                }

                R.id.itemDelete -> {
                    viewModel.deleteCategory(category)
                    viewModel.getListCategory()
                }
            }
            false
        }

        popupMenu.show()
    }

    private fun dialogAddCategory(item: Category?) {
        binding.run {
            dialogBinding.run {
                if (item != null) {
                    edtInputCate.setText(item.title)
                    tvSave.setOnSingleClickListener {
                        val category = edtInputCate.text.toString()
                        viewModel.updateCategory(Category(id = item.id, title = category), "title")
                        viewModel.getListCategory()
                        alertDialog.dismiss()
                    }
                } else {
                    tvSave.setOnSingleClickListener {
                        val category = edtInputCate.text.toString()
                        viewModel.addNewCategory(Category(title = category))
                        viewModel.getListCategory()
                        alertDialog.dismiss()
                    }
                }
                tvCancel.setOnSingleClickListener {
                    alertDialog.dismiss()
                }
            }
        }
        alertDialog.show()
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityCategoryBinding =
        ActivityCategoryBinding.inflate(inflater)
}