package com.hoanv.notetimeplanner.ui.main.person

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {

    private val _listCategories = MutableLiveData<List<Category>>()
    val listCategories: LiveData<List<Category>>
        get() = _listCategories

    init {
        getListCategory()
    }

    fun getListCategory() = viewModelScope.launch(Dispatchers.IO) {
        remoteRepo.getListCategory() { list, state ->
            if (state) {
                _listCategories.postValue(list)
            } else {
                _listCategories.postValue(emptyList())
            }
        }
    }
}