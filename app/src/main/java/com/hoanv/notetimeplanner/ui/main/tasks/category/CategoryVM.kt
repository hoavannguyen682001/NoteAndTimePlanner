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

    private val _updateCategoryTriggerS =
        MutableLiveData<ResponseState<String>>()
    val updateCategoryTriggerS: LiveData<ResponseState<String>>
        get() = _updateCategoryTriggerS

    private val _deleteCategoryTriggerS =
        MutableLiveData<ResponseState<String>>()
    val deleteCategoryTriggerS: LiveData<ResponseState<String>>
        get() = _deleteCategoryTriggerS

    init {
        getListCategory()
    }

    fun addNewCategory(category: Category) {
        _addCategoryTriggerS.value = ResponseState.Start
        remoteRepo.addNewCategory(category) {
            if (it) {
                _addCategoryTriggerS.value = ResponseState.Success("Tạo danh mục mới thành công.")
            } else {
                _addCategoryTriggerS.value =
                    ResponseState.Failure(Throwable("Tạo danh mục mới thất bại. Hãy thử lại !!"))
            }
        }
    }

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

    fun updateCategory(category: Category, field: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateCategoryTriggerS.postValue(ResponseState.Start)
            remoteRepo.updateCategory(category, field) {
                if (it) {
                    _updateCategoryTriggerS.postValue(ResponseState.Success("Chỉnh sửa thể loại thành công."))
                } else {
                    _updateCategoryTriggerS.postValue(
                        ResponseState.Failure(Throwable("Chỉnh sửa thể loại thất bại. Thử lại sau !!"))
                    )
                }
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            _deleteCategoryTriggerS.postValue(ResponseState.Start)
            remoteRepo.deleteCategory(category) {
                if (it) {
                    _deleteCategoryTriggerS.postValue(ResponseState.Success("Xoá thể loại thành công."))
                } else {
                    _deleteCategoryTriggerS.postValue(
                        ResponseState.Failure(Throwable("Xoá thể loại thất bại. Thử lại sau !!"))
                    )
                }
            }
        }
    }
}