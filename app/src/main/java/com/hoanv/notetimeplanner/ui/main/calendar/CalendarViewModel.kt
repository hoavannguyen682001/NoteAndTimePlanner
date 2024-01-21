package com.hoanv.notetimeplanner.ui.main.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.TypeTask
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    val remoteRepo: RemoteRepo
) : BaseViewModel() {

    private val _listTaskPersonal =
        MutableLiveData<ResponseState<List<Task>>>()
    val listTaskPersonal: LiveData<ResponseState<List<Task>>>
        get() = _listTaskPersonal

    private val _listGroupTask =
        MutableLiveData<ResponseState<List<Task>>>()
    val listGroupTask: LiveData<ResponseState<List<Task>>>
        get() = _listGroupTask

    private val _deleteTaskTriggerS =
        MutableLiveData<ResponseState<String>>()
    val deleteTaskTriggerS: LiveData<ResponseState<String>>
        get() = _deleteTaskTriggerS

    private val _updateTaskTriggerS =
        MutableLiveData<ResponseState<String>>()
    val updateTaskTriggerS: LiveData<ResponseState<String>>
        get() = _updateTaskTriggerS
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

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateTaskTriggerS.postValue(ResponseState.Start)
            remoteRepo.updateTask(task) {
                if (it) {
                    _updateTaskTriggerS.postValue(ResponseState.Success("Chỉnh sửa công việc thành công."))
                } else {
                    _updateTaskTriggerS.postValue(
                        ResponseState.Failure(Throwable("Chỉnh sửa thất bại. Thử lại sau !!"))
                    )
                }
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            _deleteTaskTriggerS.postValue(ResponseState.Start)
            remoteRepo.deleteTask(task) {
                if (it) {
                    _deleteTaskTriggerS.postValue(ResponseState.Success("Xoá công việc thành công."))
                } else {
                    _deleteTaskTriggerS.postValue(
                        ResponseState.Failure(Throwable("Xoá công việc thất bại. Thử lại sau !!"))
                    )
                }
            }
        }
    }
}