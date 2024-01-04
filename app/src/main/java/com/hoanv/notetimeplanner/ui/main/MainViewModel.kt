package com.hoanv.notetimeplanner.ui.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {

    private val _accessToken = MutableSharedFlow<ResponseState<String>>(extraBufferCapacity = 64)
    val accessToken = _accessToken.asSharedFlow()

    init {
        getDeviceToken()
    }

    fun getAccessToken(scopes: MutableList<String>, path: InputStream) {
        remoteRepo.getAccessToken(scopes, path)
            .map {
                Log.d("TAGGGGGGGGGGGG", "service-account $it")
                Pref.accessToken = it
                ResponseState.Success(it) as ResponseState<String>
            }.onStart {
                emit(ResponseState.Start)
            }.catch {
                emit(ResponseState.Failure(it))
            }.onEach(_accessToken::tryEmit)
            .launchIn(viewModelScope)
    }

    private fun getDeviceToken() {
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepo.getDeviceToken {
                Pref.deviceToken = it
                Log.d("TAGGGGGGGGGGGG", "device token $it")
            }
        }
    }
}