package com.hoanv.notetimeplanner.ui.main.tasks.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Todo
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
        MutableLiveData<ResponseState<List<Todo>>>()
    val listTask: LiveData<ResponseState<List<Todo>>>
        get() = _listTask


    private val _deleteTaskTriggerS =
        MutableLiveData<ResponseState<String>>()
    val deleteTaskTriggerS: LiveData<ResponseState<String>>
        get() = _deleteTaskTriggerS

    fun getListCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            _listCategory.postValue(ResponseState.Start)
            remoteRepo.getListCategory { list, state ->
                if (state) {
                    _listCategory.postValue(ResponseState.Success(list.toMutableList()))
                } else {
                    _listCategory.postValue(
                        ResponseState.Failure(Throwable("Không tìm thấy dữ liệu. Thử lại sau !!"))
                    )
                }
            }
        }
    }

    fun getListTask() {
        viewModelScope.launch(Dispatchers.IO) {
            _listTask.postValue(ResponseState.Start)
            remoteRepo.getListTask { list, state ->
                if (state) {
                    _listTask.postValue(ResponseState.Success(list.toMutableList()))
                } else {
                    _listTask.postValue(
                        ResponseState.Failure(Throwable("Không tìm thấy dữ liệu. Thử lại sau !!"))
                    )
                }
            }
        }
    }


    fun deleteCategory(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            _deleteTaskTriggerS.postValue(ResponseState.Start)
            remoteRepo.deleteTask(todo) {
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