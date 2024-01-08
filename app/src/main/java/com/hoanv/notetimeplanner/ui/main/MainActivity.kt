package com.hoanv.notetimeplanner.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.databinding.ActivityMainBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.home.category.CategoryActivity
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.flow.collectIn
import com.hoanv.notetimeplanner.utils.extension.safeClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    private val mainAdapter by lazy(LazyThreadSafetyMode.NONE) {
        MainPagerAdapter(supportFragmentManager, lifecycle)
    }
    override val viewModel: MainViewModel by viewModels()
    private val TAG = "TAGGGGGGGGGGGGGGGGGGGGG"
    private val scopes = mutableListOf(
        "https://www.googleapis.com/auth/firebase.messaging"
    )

    override fun init(savedInstanceState: Bundle?) = binding.run {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        val filePath = assets.open("service_account.json")
        viewModel.getAccessToken(scopes, filePath)

        binding.run {
            vpMainPager.run {
                adapter = mainAdapter
                offscreenPageLimit = 4
                isUserInputEnabled = false
                isSaveEnabled = false
            }
        }
    }

    private fun initListener() {
        binding.run {
            bottomNavView.setOnItemSelectedListener {
                setViewPagerSelected(it.itemId)
                true
            }
            fabAdd.safeClickListener {
                startActivity(Intent(this@MainActivity, CategoryActivity::class.java))
            }
        }
    }

    private fun bindViewModel() {
        viewModel.run {
            accessToken.collectIn(this@MainActivity) { state ->
                when (state) {
                    ResponseState.Start -> {
                    }

                    is ResponseState.Success -> {
                        Pref.accessToken = state.data
                        Log.d(TAG, Pref.accessToken)
                    }

                    is ResponseState.Failure -> {
                        toastError(state.throwable?.message)
                        Log.d("###", "${state.throwable?.message}")
                    }
                }
            }
        }
    }

    private fun setViewPagerSelected(item: Int) = binding.run {
        when (item) {
            R.id.itemTask -> vpMainPager.setCurrentItem(0, false)
            R.id.itemCalendar -> vpMainPager.setCurrentItem(1, false)
            R.id.itTemp -> vpMainPager.setCurrentItem(2, false)
            R.id.itemPerson -> vpMainPager.setCurrentItem(3, false)
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityMainBinding =
        ActivityMainBinding.inflate(inflater)
}