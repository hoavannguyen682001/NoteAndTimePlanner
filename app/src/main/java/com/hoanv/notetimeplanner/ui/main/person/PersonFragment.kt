package com.hoanv.notetimeplanner.ui.main.person

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.databinding.FragmentPersonBinding
import com.hoanv.notetimeplanner.ui.base.BaseFragment
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonFragment : BaseFragment<FragmentPersonBinding, PersonViewModel>() {
    override val viewModel: PersonViewModel by viewModels()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPersonBinding = FragmentPersonBinding.inflate(inflater, container, false)

    override fun init(savedInstanceState: Bundle?) {
        bindViewModel()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModel.getUserInfo(currentUser.email!!)
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                userInfo.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        ResponseState.Start -> {}

                        is ResponseState.Success -> {
                            val user = state.data
                            tvUserName.text = getString(R.string.hello_user, user.userName)
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
}