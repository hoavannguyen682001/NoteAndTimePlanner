package com.hoanv.notetimeplanner.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseActivity<ViewBindingType : ViewBinding, ViewModelType : BaseViewModel> :
    AppCompatActivity(),
    ViewBindingHolder<ViewBindingType> by ViewBindingHolderImpl() {

    protected abstract val viewModel: ViewModelType

    protected val binding: ViewBindingType
        get() = requireBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            initBinding(
                binding = setupViewBinding(layoutInflater),
                lifecycle = lifecycle,
                className = this::class.simpleName,
                onBound = null
            )
        )
        init(savedInstanceState)
    }

    abstract fun init(savedInstanceState: Bundle?)

    abstract fun setupViewBinding(inflater: LayoutInflater): ViewBindingType

    fun toastInfo(content: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            content?.let { Toasty.info(this@BaseActivity, content, Toast.LENGTH_SHORT).show() }
        }
    }

    fun toastSuccess(content: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            content?.let { Toasty.success(this@BaseActivity, content, Toast.LENGTH_SHORT).show() }
        }
    }

    fun toastError(content: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            content?.let { Toasty.error(this@BaseActivity, content, Toast.LENGTH_SHORT).show() }
        }
    }

    fun toastWarning(content: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            content?.let { Toasty.warning(this@BaseActivity, content, Toast.LENGTH_SHORT).show() }
        }
    }
}