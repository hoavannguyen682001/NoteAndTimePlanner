package com.hoanv.notetimeplanner.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseFragment<ViewBindingType : ViewBinding, ViewModelType : BaseViewModel> :
    Fragment(),
    ViewBindingHolder<ViewBindingType> by ViewBindingHolderImpl() {

    abstract val viewModel: ViewModelType

    private var _mContext: Context? = null
    protected val mContext: Context
        get() = requireNotNull(_mContext)

    protected val binding: ViewBindingType
        get() = requireBinding()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initBinding(
        binding = setupViewBinding(inflater, container),
        lifecycle = viewLifecycleOwner.lifecycle,
        className = this::class.simpleName,
        onBound = null
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    abstract fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): ViewBindingType

    abstract fun init(savedInstanceState: Bundle?)

    override fun onDetach() {
        _mContext = null
        super.onDetach()
    }

    protected val baseActivity: BaseActivity<*, *>?
        get() = activity as BaseActivity<*, *>?

    fun toastInfo(content: String?) {
        baseActivity?.toastInfo(content)
    }

    fun toastSuccess(content: String?) {
        baseActivity?.toastSuccess(content)
    }

    fun toastWarning(content: String?) {
        baseActivity?.toastWarning(content)
    }

    fun toastError(content: String?) {
        baseActivity?.toastError(content)
    }
}