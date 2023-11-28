package com.hoanv.notetimeplanner.ui.main.tasks.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryVM
@Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {

    private val _addCategoryTriggerS = MutableLiveData<ResponseState<String>>()
    val addCategoryTriggerS: LiveData<ResponseState<String>>
        get() = _addCategoryTriggerS

    private val _listCategory =
        MutableLiveData<ResponseState<List<Category>>>()
    val listCategory: LiveData<ResponseState<List<Category>>>
        get() = _listCategory

    init {
        getListCategory()
    }

    fun addNewCategory(category: Category) {
        _addCategoryTriggerS.value = ResponseState.Start
        remoteRepo.addNewCategory(category) {
            if (it) {
                _addCategoryTriggerS.value = ResponseState.Success("Tạo danh mục mới thành công")
            } else {
                _addCategoryTriggerS.value =
                    ResponseState.Failure(Throwable("Tạo danh mục mới thất bại. Hãy thử lại !!"))
            }
        }
    }

    fun getListCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            _listCategory.postValue(ResponseState.Start)
            remoteRepo.getListCategory {
                if (it.isNotEmpty()) {
                    _listCategory.postValue(ResponseState.Success(it.toMutableList()))
                } else {
                    _listCategory.postValue(
                        ResponseState.Failure(Throwable("Không tìm thấy dữ liệu. Thử lại sau !!"))
                    )
                }
            }
        }
    }
}