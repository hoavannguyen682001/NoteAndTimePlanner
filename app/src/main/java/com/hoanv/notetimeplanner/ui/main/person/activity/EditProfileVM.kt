package com.hoanv.notetimeplanner.ui.main.person.activity

import android.net.Uri
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
import javax.inject.Inject

@HiltViewModel
class EditProfileVM @Inject constructor(
    val remoteRepo: RemoteRepo
) : BaseViewModel() {

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String>
        get() = _imageUrl

    private val _updateTriggerS = MutableLiveData<ResponseState<String>>()
    val updateTriggerS: LiveData<ResponseState<String>>
        get() = _updateTriggerS

    fun updateUserInfo(userInfo: UserInfo) = viewModelScope.launch(Dispatchers.IO) {
        _updateTriggerS.postValue(ResponseState.Start)
        remoteRepo.updateUserInfo(userInfo) {
            if (it) {
                _updateTriggerS.postValue(ResponseState.Success("Cập nhật thông tin thành công."))
            } else {
                _updateTriggerS.postValue(ResponseState.Failure(Throwable("Cập nhật thông tin thất bại. Thử lại sau!")))
            }
        }
    }

    fun upLoadAvatar(imageUri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        remoteRepo.uploadAvatar(Pref.userId, imageUri) {
            _imageUrl.postValue(it)
        }
    }
}