package com.hoanv.notetimeplanner.ui.main.login.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginVM @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {
    private val _signInTriggerS = MutableLiveData<ResponseState<String>>()
    val signInTriggerS: LiveData<ResponseState<String>>
        get() = _signInTriggerS

    fun signInUserAccount(userInfo: UserInfo) = viewModelScope.launch(Dispatchers.IO) {
        _signInTriggerS.postValue(ResponseState.Start)
        remoteRepo.signInUserAccount(userInfo) {
            if (it) {
                _signInTriggerS.postValue(ResponseState.Success("Thành công."))
            } else {
                _signInTriggerS.postValue(ResponseState.Failure(Throwable("Xoá công việc thất bại. Thử lại sau !!")))
            }
        }
    }
}