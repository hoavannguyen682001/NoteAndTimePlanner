package com.hoanv.notetimeplanner.ui.main.login.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.databinding.ActivityRegisterBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.main.MainActivity
import com.hoanv.notetimeplanner.ui.main.login.signin.LoginActivity
import com.hoanv.notetimeplanner.utils.FieldValidators
import com.hoanv.notetimeplanner.utils.Pref
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
                val isValidate = validateUserName() && validateEmail() && validatePassword()
                if (isValidate) {
                    createUserAccount()
                }
            }

            ivGoogle.setOnSingleClickListener {
                signInWithGoogle()
            }

            edtUserName.addTextChangedListener(TextFieldValidation(edtUserName))
            edtEmail.addTextChangedListener(TextFieldValidation(edtEmail))
            edtPassword.addTextChangedListener(TextFieldValidation(edtPassword))
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                registerTriggerS.observe(this@RegisterActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {
                            showLoadingDialog()
                        }

                        is ResponseState.Success -> {
                            dismissLoadingDialog()
                            startActivity(
                                Intent(
                                    this@RegisterActivity,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        }

                        is ResponseState.Failure -> {
                            dismissLoadingDialog()
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
                userToken = Pref.deviceToken
            )
            Log.d("USER_TOKEN", "${Pref.deviceToken} - ${userInfo.userToken}")

            viewModel.createUserAccount(userInfo)
        }
    }

    private val signInGoogleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account.idToken?.let {
                        viewModel.signInWithGoogle(it)
                    }
                } catch (e: ApiException) {
                    toastError("Google sign in failed: ${e.message}")
                }
            }
        }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        signInGoogleLauncher.launch(signInIntent)
    }

    /**
     * field must not be empty
     */
    private fun validateUserName(): Boolean {
        binding.run {
            val text = edtUserName.text.toString().trim()
            if (text.isEmpty()) {
                tilUserName.isErrorEnabled = true
                tilUserName.error = "Vui lòng nhập tên của bạn"
                return false
            } else {
                tilUserName.isErrorEnabled = false
            }
            return true
        }
    }

    /**
     *  field must not be empty
     *  text should matches email address format
     */
    private fun validateEmail(): Boolean {
        binding.run {
            val text = edtEmail.text.toString().trim()
            if (text.isEmpty() || !FieldValidators.isValidEmail(text)) {
                tilEmail.isErrorEnabled = true
                tilEmail.error = "Vui lòng nhập đúng format: example@gmail.com"
                return false
            } else {
                tilEmail.isErrorEnabled = false
            }
            return true
        }
    }

    /**
     *  field must not be empty
     *  password length must not be less than 6
     */
    private fun validatePassword(): Boolean {
        binding.run {
            val text = edtPassword.text.toString().trim()
            if (text.isEmpty() || text.length < 6) {
                tilPassword.isErrorEnabled = true
                tilPassword.error = "Vui lòng nhập mật khẩu lớn hơn 6 ký tự !"
                return false
            } else {
                tilPassword.isErrorEnabled = false
            }
            return true
        }
    }

    /**
     * applying text watcher on each text field
     */
    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // checking ids of each text field and applying functions accordingly.
            when (view.id) {
                R.id.edtUserName -> {
                    validateUserName()
                }

                R.id.edtEmail -> {
                    validateEmail()
                }

                R.id.edtPassword -> {
                    validatePassword()
                }
            }
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityRegisterBinding =
        ActivityRegisterBinding.inflate(inflater)
}