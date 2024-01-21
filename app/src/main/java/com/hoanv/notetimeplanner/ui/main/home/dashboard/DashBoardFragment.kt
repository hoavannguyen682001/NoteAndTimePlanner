package com.hoanv.notetimeplanner.ui.main.home.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.TypeTask
import com.hoanv.notetimeplanner.databinding.FragmentDashboardBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.main.home.category.CategoryActivity
import com.hoanv.notetimeplanner.ui.main.listTask.ListAllTaskActivity
import com.hoanv.notetimeplanner.utils.AppConstant
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectInViewLifecycle
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
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
        bindViewModel()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getListTask()
        viewModel.getListCategory()
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
                startActivity(Intent(requireContext(), ListAllTaskActivity::class.java).apply {
                    putExtra(AppConstant.TASK_TYPE, TypeTask.PERSONAL.name)
                })
            }

            cslGroupTask.setOnSingleClickListener {
                startActivity(Intent(requireContext(), ListAllTaskActivity::class.java).apply {
                    putExtra(AppConstant.TASK_TYPE, TypeTask.GROUP.name)
                })
            }

            cslStatistical.setOnSingleClickListener {
//                startActivity(Intent(requireContext(), CategoryActivity::class.java))
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                listTaskPersonal.asFlow().collectInViewLifecycle(this@DashBoardFragment) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            tvTaskNumb1.text = state.data.size.toString() + " Công việc"
                        }

                        is ResponseState.Failure -> {
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listGroupTask.asFlow().collectInViewLifecycle(this@DashBoardFragment) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            tvTaskNumb.text = state.data.size.toString() + " Dự án"
                            tvTaskNumb2.text = sumOfTask.toString() + " Công việc"
                        }

                        is ResponseState.Failure -> {
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listCategory.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        ResponseState.Start -> {
                        }

                        is ResponseState.Success -> {
                            tvTaskNumb3.text = state.data.size.toString() + " Thể loại"
                        }

                        is ResponseState.Failure -> {
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }
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