package com.hoanv.notetimeplanner.ui.main.login.register

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
                _registerTriggerS.postValue(ResponseState.Failure(Throwable("Xoá công việc thất bại. Thử lại sau !!")))
            }
        }
    }
}