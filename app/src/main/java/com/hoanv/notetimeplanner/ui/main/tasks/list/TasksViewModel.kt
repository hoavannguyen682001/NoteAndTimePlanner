package com.hoanv.notetimeplanner.ui.main.tasks.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {
    private val _listCategory =
        MutableLiveData<ResponseState<List<Category>>>()
    val listCategory: LiveData<ResponseState<List<Category>>>
        get() = _listCategory

    private val _listTask =
        MutableLiveData<ResponseState<List<Task>>>()
    val listTask: LiveData<ResponseState<List<Task>>>
        get() = _listTask

    private val _deleteTaskTriggerS =
        MutableLiveData<ResponseState<String>>()
    val deleteTaskTriggerS: LiveData<ResponseState<String>>
        get() = _deleteTaskTriggerS

    private val _updateTaskTriggerS =
        MutableLiveData<ResponseState<String>>()
    val updateTaskTriggerS: LiveData<ResponseState<String>>
        get() = _updateTaskTriggerS

    fun getListCategory() {
        _listCategory.postValue(ResponseState.Start)
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepo.getListCategory { list, state ->
                if (state) {
                    _listCategory.postValue(ResponseState.Success(list.toMutableList()))
                    Log.d("CategoryList", "$list")
                } else {
                    _listCategory.postValue(
                        ResponseState.Failure(Throwable("Không tìm thấy dữ liệu. Thử lại sau !!"))
                    )
                }
            }
        }
    }

    fun deleteCategory(task: Task) {
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

    fun getListTaskByCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            _listTask.postValue(ResponseState.Start)
            remoteRepo.getListTaskByCategory(category) { list, state ->
                if (state) {
                    _listTask.postValue(ResponseState.Success(list.toMutableList()))
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