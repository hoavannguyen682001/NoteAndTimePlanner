package com.hoanv.notetimeplanner.ui.main.listTask

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListAllTaskVM @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {
    private val _listTask =
        MutableLiveData<ResponseState<List<Task>>>()
    val listTask: LiveData<ResponseState<List<Task>>>
        get() = _listTask

    init {
        getListTask()
    }

    fun getListTask() {
        _listTask.postValue(ResponseState.Start)
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepo.getListTask { list, state ->
                if (state) {
                    _listTask.postValue(ResponseState.Success(list))
                    Log.d("MuTaBleList", "$list")
                } else {
                    _listTask.postValue(
                        ResponseState.Failure(Throwable("Không tìm thấy dữ liệu. Thử lại sau !!"))
                    )
                }
            }
        }
    }
}