package com.hoanv.notetimeplanner.ui.main.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hoanv.notetimeplanner.databinding.FragmentPersonBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonFragment : BaseFragment<FragmentPersonBinding, PersonViewModel>() {
    override val viewModel: PersonViewModel by viewModels()

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPersonBinding = FragmentPersonBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {

    }
}