package com.hoanv.notetimeplanner.ui.main.listTask

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.TypeTask
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
class ListAllTaskVM @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {

    private val _listTaskPersonal =
        MutableLiveData<ResponseState<List<Task>>>()
    val listTaskPersonal: LiveData<ResponseState<List<Task>>>
        get() = _listTaskPersonal

    private val _listGroupTask =
        MutableLiveData<ResponseState<List<Task>>>()
    val listGroupTask: LiveData<ResponseState<List<Task>>>
        get() = _listGroupTask

    init {
        getListTask()
    }

    fun getListTask() {
        _listTaskPersonal.postValue(ResponseState.Start)
        _listGroupTask.postValue(ResponseState.Start)
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepo.getListTask { list, state ->
                if (state) {
                    val task = mutableListOf<Task>()
                    val group = mutableListOf<Task>()

                    list.forEach {
                        if (it.typeTask == TypeTask.PERSONAL) {
                            if (Pref.userId == it.userId) {
                                task.add(it)
                            }
                        } else {
                            it.listMember.forEach { u ->
                                if (u.uid == Pref.userId) {
                                    group.add(it)
                                }
                            }

                        }
                    }
                    _listTaskPersonal.postValue(ResponseState.Success(task))
                    _listGroupTask.postValue(ResponseState.Success(group))
                } else {
                    _listTaskPersonal.postValue(
                        ResponseState.Failure(Throwable("Không tìm thấy dữ liệu. Thử lại sau !!"))
                    )
                    _listGroupTask.postValue(
                        ResponseState.Failure(Throwable("Không tìm thấy dữ liệu. Thử lại sau !!"))
                    )
                }
            }
        }
    }
}