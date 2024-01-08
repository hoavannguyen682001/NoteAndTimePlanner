package com.hoanv.notetimeplanner.ui.main.home.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.asFlow
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Icon
import com.hoanv.notetimeplanner.databinding.ActivityCategoryBinding
import com.hoanv.notetimeplanner.databinding.DialogAddCategoryBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectIn
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import com.hoanv.notetimeplanner.utils.extension.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

@AndroidEntryPoint
class CategoryActivity : BaseActivity<ActivityCategoryBinding, CategoryVM>() {
    override val viewModel: CategoryVM by viewModels()

    private val categoryAdapter by lazy {
        CategoryAdapter(this, ::handleOptionMenu)
    }

    private val iconAdapter by lazy {
        IconCategoryAdapter(this) { item, position ->
            mIcon = item
            selectedS.tryEmit(position)
        }
    }
    private var selectedS = MutableStateFlow(0)

    private lateinit var dialogBinding: DialogAddCategoryBinding
    private lateinit var alertDialog: AlertDialog
    private lateinit var mIcon: Icon

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
                    itemAnimator = null
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
                val listIcon = mutableListOf<Icon>()

                selectedS.combine(iconCategory.asFlow()) { selected, list -> Pair(selected, list) }
                    .collectIn(this@CategoryActivity) { pair ->
                        val (select, listUrl) = pair

                        listIcon.clear()
                        listUrl.forEachIndexed { index, url ->
                            listIcon.add(Icon(iconUrl = url, isSelected = select == index))
                        }

                        //TODO bug when scroll to end and click item icon
                        iconAdapter.submitList(listIcon.map { it.ownCopy() })
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
                        viewModel.updateCategory(
                            Category(id = item.id, title = category, icon = mIcon),
                            "title"
                        )
                        viewModel.getListCategory()
                        alertDialog.dismiss()
                    }
                } else {
                    tvSave.setOnSingleClickListener {
                        val category = edtInputCate.text.toString()
                        viewModel.addNewCategory(Category(title = category, icon = mIcon))
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