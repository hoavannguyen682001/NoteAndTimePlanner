package com.hoanv.notetimeplanner.ui.main.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hoanv.notetimeplanner.databinding.FragmentCalendarBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalendarFragment: BaseFragment<FragmentCalendarBinding, CalendarViewModel>() {
    override val viewModel: CalendarViewModel by viewModels()

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCalendarBinding =
        FragmentCalendarBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {

    }
}