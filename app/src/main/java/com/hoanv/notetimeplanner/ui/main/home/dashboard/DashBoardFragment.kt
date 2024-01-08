package com.hoanv.notetimeplanner.ui.main.home.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.databinding.FragmentDashboardBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.main.home.category.CategoryActivity
import com.hoanv.notetimeplanner.ui.main.listTask.ListAllTaskActivity
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashBoardFragment : BaseFragment<FragmentDashboardBinding, DashBoardVM>() {
    override val viewModel: DashBoardVM by viewModels()

    private val date = Calendar.getInstance()

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDashboardBinding = FragmentDashboardBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
    }

    private fun initView() {
        binding.run {
            val today = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
            val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
            val month = date.get(Calendar.MONTH)
            val year = date.get(Calendar.YEAR)

            tvDayQuote.text = getString(
                R.string.day_quote,
                formatWeekToString(dayOfWeek),
                today.toString(),
                formatMonthToString(month),
            )
            tvYear.text = year.toString()
        }
    }

    private fun initListener() {
        binding.run {
            cslCategory.setOnSingleClickListener {
                startActivity(Intent(requireContext(), CategoryActivity::class.java))
            }

            cslTask.setOnSingleClickListener {
                startActivity(Intent(requireContext(), ListAllTaskActivity::class.java))
            }

            cslGroupTask.setOnSingleClickListener {
                startActivity(Intent(requireContext(), CategoryActivity::class.java))
            }

            cslStatistical.setOnSingleClickListener {
                startActivity(Intent(requireContext(), CategoryActivity::class.java))
            }
        }
    }

    private fun formatMonthToString(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "Tháng Một"
            Calendar.FEBRUARY -> "Tháng Hai"
            Calendar.MARCH -> "Tháng Ba"
            Calendar.APRIL -> "Tháng Bốn"
            Calendar.MAY -> "Tháng Năm"
            Calendar.JUNE -> "Tháng Sáu"
            Calendar.JULY -> "Tháng Bảy"
            Calendar.AUGUST -> "Tháng Tám"
            Calendar.SEPTEMBER -> "Tháng Chín"
            Calendar.OCTOBER -> "Tháng Mười"
            Calendar.NOVEMBER -> "Tháng Mười Một"
            Calendar.DECEMBER -> "Tháng Mười Hai"
            else -> ""
        }
    }

    private fun formatWeekToString(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Chủ Nhật"
            Calendar.MONDAY -> "Thứ Hai"
            Calendar.TUESDAY -> "Thứ Ba"
            Calendar.WEDNESDAY -> "Thứ Tư"
            Calendar.THURSDAY -> "Thứ Năm"
            Calendar.FRIDAY -> "Thứ Sáu"
            Calendar.SATURDAY -> "Thứ Bảy"
            else -> ""
        }
    }
}