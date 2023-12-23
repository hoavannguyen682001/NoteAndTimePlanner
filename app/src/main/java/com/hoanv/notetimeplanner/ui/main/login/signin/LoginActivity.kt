package com.hoanv.notetimeplanner.ui.main.login.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.databinding.ActivityLoginBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.MainActivity
import com.hoanv.notetimeplanner.ui.main.login.register.RegisterActivity
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginVM>() {
    override val viewModel: LoginVM by viewModels()

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
    }

    private fun initListener() {
        binding.run {
            tvSignUp.setOnSingleClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }

            ivPassCheck.setOnClickListener {
                ivPassCheck.isSelected = !ivPassCheck.isSelected
            }

            tvLogin.setOnSingleClickListener {
                loginUserAccount()
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                signInTriggerS.observe(this@LoginActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {}

                        is ResponseState.Success -> {
                            toastSuccess(state.data)
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }
            }
        }
    }

    private fun loginUserAccount() {
        val userInfo = UserInfo(
            userEmail = binding.edtEmail.text.toString(),
            userPassword = binding.edtPassword.text.toString(),
        )
        viewModel.signInUserAccount(userInfo)
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityLoginBinding =
        ActivityLoginBinding.inflate(inflater)
}