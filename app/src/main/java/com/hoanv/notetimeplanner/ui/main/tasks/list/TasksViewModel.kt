package com.hoanv.notetimeplanner.ui.main.tasks.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.hoanv.notetimeplanner.Application
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
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
}