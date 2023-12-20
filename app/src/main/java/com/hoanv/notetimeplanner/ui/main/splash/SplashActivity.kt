package com.hoanv.notetimeplanner.ui.main.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.hoanv.notetimeplanner.databinding.ActivitySplashBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.MainActivity
import com.hoanv.notetimeplanner.utils.extension.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashVM>() {
    override val viewModel: SplashVM
        get() = SplashVM()

    override fun init(savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() = binding.run {
        binding.lottieAnim.visible()
        lifecycleScope.launch {
            delay(3000)
            gotoMain()
        }
    }

    private fun gotoMain() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivitySplashBinding =
        ActivitySplashBinding.inflate(inflater)
}