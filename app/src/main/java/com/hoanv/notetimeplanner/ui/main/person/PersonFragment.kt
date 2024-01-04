package com.hoanv.notetimeplanner.ui.main.person

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.databinding.FragmentPersonBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.ui.main.person.activity.EditProfileActivity
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import fxc.dev.common.extension.resourceColor
import kotlin.math.roundToInt

@AndroidEntryPoint
class PersonFragment : BaseFragment<FragmentPersonBinding, PersonViewModel>() {
    override val viewModel: PersonViewModel by viewModels()

    private val listCategoriesS = mutableListOf<Category>()
    private var user = UserInfo()
    override fun setupViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentPersonBinding = FragmentPersonBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        binding.run {
        }
    }

    private fun initListener() {
        binding.run {
            ivProfile.setOnSingleClickListener {
                startActivity(
                    Intent(requireActivity(), EditProfileActivity::class.java).apply {
                        putExtra("USER", user)
                    }
                )
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                userInfo.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        ResponseState.Start -> {}

                        is ResponseState.Success -> {
                            user = state.data
                            tvUserName.text = getString(R.string.hello_user, user.userName)
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }

                listCategories.observe(viewLifecycleOwner) {
                    listCategoriesS.addAll(it)
                    setupPieChart()
                }
            }
        }
    }

    private fun setupPieChart() {
        val colors = mutableListOf(
            resourceColor(R.color.colorPrimary),
            resourceColor(R.color.picton_blue),
        )

        binding.run {
            val list = ArrayList<PieEntry>()
            listCategoriesS.forEach {
                list.add(PieEntry(it.listTask.toFloat(), it.title))
            }

            val pieDataSet = PieDataSet(list, "")

            pieDataSet.colors = colors
            pieDataSet.valueTextColor = Color.BLACK
            pieDataSet.valueTextSize = 12f
            pieDataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.roundToInt()}"
                }
            }

            pieChartStatistical.run {
                data = PieData(pieDataSet)
                legend.isEnabled = false
                setDrawEntryLabels(false)
                description.isEnabled = false
                animateY(1000)
                invalidate()
            }
        }
    }
}