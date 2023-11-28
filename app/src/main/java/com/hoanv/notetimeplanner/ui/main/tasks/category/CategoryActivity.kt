package com.hoanv.notetimeplanner.ui.main.tasks.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.databinding.ActivityCategoryBinding
import com.hoanv.notetimeplanner.databinding.DialogAddCategoryBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectIn
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import com.hoanv.notetimeplanner.utils.extension.visible
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class CategoryActivity : BaseActivity<ActivityCategoryBinding, CategoryVM>() {
    override val viewModel: CategoryVM by viewModels()

    private val categoryAdapter by lazy {
        CategoryAdapter(this)
    }

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.run {
            rvCategory.run {
                layoutManager = LinearLayoutManager(
                    this@CategoryActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                adapter = categoryAdapter
            }
        }
    }

    private fun initListener() {
        binding.run {
            tvAddCategory.setOnSingleClickListener {
                dialogAddCategory()
            }
            ivBack.setOnSingleClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
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
            }
        }
    }

    private fun dialogAddCategory() {
        val dialogBinding = DialogAddCategoryBinding.inflate(LayoutInflater.from(this))
        val alertDialog =
            AlertDialog.Builder(this, R.style.AppCompat_AlertDialog)
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()

        binding.run {
            dialogBinding.run {
                tvSave.setOnSingleClickListener {
                    val category = edtInputCate.text.toString()
                    viewModel.addNewCategory(Category(title = category))
                    alertDialog.dismiss()
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