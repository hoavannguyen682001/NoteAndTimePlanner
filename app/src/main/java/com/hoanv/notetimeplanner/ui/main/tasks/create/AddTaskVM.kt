package com.hoanv.notetimeplanner.ui.main.tasks.create

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

    fun addNewTask(todo: Todo) {
        _addTaskTriggerS.value = ResponseState.Start
        remoteRepo.addNewTask(todo) {
            if (it) {
                _addTaskTriggerS.value = ResponseState.Success("Tạo công việc mới thành công.")
            } else {
                _addTaskTriggerS.value =
                    ResponseState.Failure(Throwable("Tạo công việc mới thất bại. Hãy thử lại !!"))
            }
        }
    }

    fun updateCategory(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateTaskTriggerS.postValue(ResponseState.Start)
            remoteRepo.updateTask(todo) {
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
}