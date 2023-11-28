package com.hoanv.notetimeplanner.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.databinding.ActivityMainBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.tasks.create.AddTaskActivity
import com.hoanv.notetimeplanner.utils.extension.safeClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    private val mainAdapter by lazy(LazyThreadSafetyMode.NONE) {
        MainPagerAdapter(supportFragmentManager, lifecycle)
    }
    override val viewModel: MainViewModel by viewModels()

    override fun init(savedInstanceState: Bundle?) = binding.run {
        vpMainPager.run {
            adapter = mainAdapter
            offscreenPageLimit = 4
            isUserInputEnabled = false
            isSaveEnabled = false
        }
        bottomNavView.setOnItemSelectedListener {
            setViewPagerSelected(it.itemId)
            true
        }
        fabAdd.safeClickListener {
            startActivity(Intent(this@MainActivity, AddTaskActivity::class.java))
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