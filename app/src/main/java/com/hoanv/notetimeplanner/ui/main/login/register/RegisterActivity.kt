package com.hoanv.notetimeplanner.ui.main.login.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.databinding.ActivityRegisterBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.MainActivity
import com.hoanv.notetimeplanner.ui.main.login.signin.LoginActivity
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : BaseActivity<ActivityRegisterBinding, RegisterVM>() {
    override val viewModel: RegisterVM by viewModels()

    override fun init(savedInstanceState: Bundle?) {
        initView()
        intiListener()
        bindViewModel()
    }

    private fun initView() {
    }

    private fun intiListener() {
        binding.run {
            tvSignIn.setOnSingleClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }

            tvRegister.setOnSingleClickListener {
                createUserAccount()
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                registerTriggerS.observe(this@RegisterActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {}

                        is ResponseState.Success -> {
                            toastSuccess(state.data)
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
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

    private fun createUserAccount() {
        binding.run {
            val userInfo = UserInfo(
                userName = edtUserName.text.toString(),
                userEmail = edtEmail.text.toString(),
                userPassword = edtPassword.text.toString(),
            )

            viewModel.createUserAccount(userInfo)
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityRegisterBinding =
        ActivityRegisterBinding.inflate(inflater)
}