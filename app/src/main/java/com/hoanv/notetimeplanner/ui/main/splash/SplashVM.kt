package com.hoanv.notetimeplanner.ui.main.splash

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.Pref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashVM @Inject
constructor(
    val remoteRepo: RemoteRepo
) : BaseViewModel() {

    init {
        getDeviceToken()
    }

    private fun getDeviceToken() {
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepo.getDeviceToken {
                Pref.deviceToken = it
                Log.d("Device_Token", "device token $it")
            }
        }
    }
}