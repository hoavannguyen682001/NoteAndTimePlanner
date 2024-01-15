package com.hoanv.notetimeplanner.ui.main.login.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegisterVM @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {
    private val _registerTriggerS = MutableLiveData<ResponseState<String>>()
    val registerTriggerS: LiveData<ResponseState<String>>
        get() = _registerTriggerS

    fun createUserAccount(userInfo: UserInfo) = viewModelScope.launch(Dispatchers.IO) {
        _registerTriggerS.postValue(ResponseState.Start)
        remoteRepo.registerUserAccount(userInfo) {
            if (it) {
                _registerTriggerS.postValue(ResponseState.Success("Thành công."))
            } else {
                _registerTriggerS.postValue(ResponseState.Failure(Throwable("Thất bại. Thử lại sau !!")))
            }
        }
    }

    fun uploadUserInfo(userInfo: UserInfo) = viewModelScope.launch(Dispatchers.IO) {
        remoteRepo.createUserInfoByGoogleAuth(userInfo)
    }

    fun signInWithGoogle(idToken: String) {
        _registerTriggerS.postValue(ResponseState.Start)
        remoteRepo.signInWithGoogle(idToken) { user, state ->
            if (state && user != null) {
                val userInfo = UserInfo(
                    userName = user.displayName ?: "",
                    userEmail = user.email ?: "",
                    userToken = Pref.deviceToken,
                    userPassword = UUID.randomUUID().toString(),
                )
                /* upload user info*/
                uploadUserInfo(userInfo)

                _registerTriggerS.postValue(ResponseState.Success("Thành công."))
                Pref.isSaveLogin = true
            } else {
                _registerTriggerS.postValue(ResponseState.Failure(Throwable("Thất bại. Thử lại sau !!")))
            }
        }
    }
}