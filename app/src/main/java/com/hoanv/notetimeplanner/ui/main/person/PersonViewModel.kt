package com.hoanv.notetimeplanner.ui.main.person

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
class PersonViewModel @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {
    private val _userInfo = MutableLiveData<ResponseState<UserInfo>>()
    val userInfo: LiveData<ResponseState<UserInfo>>
        get() = _userInfo

    fun getUserInfo(email: String) = viewModelScope.launch(Dispatchers.IO) {
        _userInfo.postValue(ResponseState.Start)
        remoteRepo.getUserInfo(email) { userInfo ->
            if (userInfo != null) {
                _userInfo.postValue(ResponseState.Success(userInfo))
            } else {
                _userInfo.postValue(ResponseState.Failure(Throwable("Fail")))
            }
        }
    }
}