package com.hoanv.notetimeplanner.ui.main.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hoanv.notetimeplanner.databinding.FragmentDashboardBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment

class DashBoardFragment: BaseFragment<FragmentDashboardBinding, DashBoardVM>() {
    override val viewModel: DashBoardVM by viewModels()

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDashboardBinding  = FragmentDashboardBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {

    }


}