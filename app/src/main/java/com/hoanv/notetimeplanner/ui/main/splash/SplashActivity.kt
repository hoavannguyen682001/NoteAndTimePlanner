package com.hoanv.notetimeplanner.ui.main.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.hoanv.notetimeplanner.databinding.ActivitySplashBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.MainActivity
import com.hoanv.notetimeplanner.ui.main.login.signin.LoginActivity
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.extension.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashVM>() {
    override val viewModel: SplashVM by viewModels()

    private lateinit var auth: FirebaseAuth
    override fun init(savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() = binding.run {
        auth = FirebaseAuth.getInstance()
        binding.lottieAnim.visible()
        lifecycleScope.launch {
            delay(3000)
            gotoMain()
        }
    }

    private fun gotoMain() {
        if (auth.currentUser != null && Pref.isSaveLogin) {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivitySplashBinding =
        ActivitySplashBinding.inflate(inflater)
}