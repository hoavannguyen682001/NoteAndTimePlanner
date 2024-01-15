package com.hoanv.notetimeplanner.ui.main.home.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.FileInfo
import com.hoanv.notetimeplanner.data.models.ImageInfo
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.group.GroupNotification
import com.hoanv.notetimeplanner.data.models.group.ResponseKey
import com.hoanv.notetimeplanner.data.models.notification.NotificationData
import com.hoanv.notetimeplanner.data.models.notification.ResponseNoti
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
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
import javax.inject.Inject

@HiltViewModel
class AddTaskVM @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {
    private val _listCategory =
        MutableLiveData<List<Category>>()
    val listCategory: LiveData<List<Category>>
        get() = _listCategory

    private val _addTaskTriggerS = MutableLiveData<ResponseState<String>>()
    val addTaskTriggerS: LiveData<ResponseState<String>>
        get() = _addTaskTriggerS

    private val _updateTaskTriggerS =
        MutableLiveData<ResponseState<String>>()
    val updateTaskTriggerS: LiveData<ResponseState<String>>
        get() = _updateTaskTriggerS

    private val _uploadImageTriggerS =
        MutableLiveData<ResponseState<Task>>()
    val uploadImageTriggerS: LiveData<ResponseState<Task>>
        get() = _uploadImageTriggerS

    private val _uploadFileTriggerS =
        MutableLiveData<ResponseState<Task>>()
    val uploadFileTriggerS: LiveData<ResponseState<Task>>
        get() = _uploadFileTriggerS


    private val _detailTask = MutableLiveData<Task>()
    val detailTask: LiveData<Task>
        get() = _detailTask

    private val _sendNotiTriggerS =
        MutableSharedFlow<ResponseState<ResponseNoti>>(extraBufferCapacity = 64)
    val sendNotiTriggerS = _sendNotiTriggerS.asSharedFlow()

    init {
        getListCategory()
    }

    private fun getListCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepo.getListCategory { list, state ->
                if (state) {
                    _listCategory.postValue((list.toMutableList()))
                } else {
                    _listCategory.postValue(
                        emptyList()
                    )
                }
            }
        }
    }

    fun addNewTask(task: Task) {
        _addTaskTriggerS.value = ResponseState.Start
        remoteRepo.addNewTask(task) {
            if (it) {
                _addTaskTriggerS.value = ResponseState.Success("Tạo công việc mới thành công.")
            } else {
                _addTaskTriggerS.value =
                    ResponseState.Failure(Throwable("Tạo công việc mới thất bại. Hãy thử lại !!"))
            }
        }
    }

    fun uploadImageOfTask(task: Task, listUri: List<ImageInfo>) =
        viewModelScope.launch(Dispatchers.IO) {
            _uploadImageTriggerS.postValue(ResponseState.Start)
            remoteRepo.uploadImageOfTask(task, listUri) { task, state ->
                if (state) {
                    _uploadImageTriggerS.postValue(ResponseState.Success(task))
                } else {
                    _uploadImageTriggerS.postValue(ResponseState.Failure(Throwable("Có lỗi xảy ra. Hãy thử lại sau !!")))
                }
            }
        }

    fun uploadFileOfTask(task: Task, listUri: List<FileInfo>) =
        viewModelScope.launch(Dispatchers.IO) {
            _uploadFileTriggerS.postValue(ResponseState.Start)
            remoteRepo.uploadFileOfTask(task, listUri) { task, state ->
                if (state) {
                    _uploadFileTriggerS.postValue(ResponseState.Success(task))
                } else {
                    _uploadFileTriggerS.postValue(ResponseState.Failure(Throwable("Có lỗi xảy ra. Hãy thử lại sau !!")))
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

    fun updateCategory(category: Category, field: String) {
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepo.updateCategory(category, field) {}
        }
    }

    fun sendNotification(body: NotificationData) {
        remoteRepo.sendNotification(body)
            .map {
                Log.d("TAGGGGGGGGGGGGG", "$it")
                ResponseState.Success(it) as ResponseState<ResponseNoti>
            }.onStart {
                emit(ResponseState.Start)
            }.catch {
                Log.d("TAGGGGGGGGGGGGG", "${it.message}")
                emit(ResponseState.Failure(it))
            }.onEach(_sendNotiTriggerS::tryEmit)
            .launchIn(viewModelScope)
    }

    fun getDetailTask(taskId: String) = viewModelScope.launch(Dispatchers.IO) {
        remoteRepo.getDetailTask(taskId) {
            _detailTask.postValue(it)
        }
    }

    fun createGroupNotification(body: GroupNotification) {
        remoteRepo.createGroupNotification(body)
            .map {
                Log.d("createGroupNotification", "$it")
                ResponseState.Success(it) as ResponseState<ResponseKey>
            }.onStart {
                emit(ResponseState.Start)
            }.catch {
                Log.d("createGroupNotification", "${it.message}")
                emit(ResponseState.Failure(it))
            }.onEach {

            }
            .launchIn(viewModelScope)
    }
}