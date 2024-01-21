package com.hoanv.notetimeplanner.ui.main.person

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hoanv.notetimeplanner.data.models.Category
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
class PersonViewModel @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {

    private val _listCategories = MutableLiveData<List<Category>>()
    val listCategories: LiveData<List<Category>>
        get() = _listCategories

    private val _listTask =
        MutableLiveData<ResponseState<List<Task>>>()
    val listTask: LiveData<ResponseState<List<Task>>>
        get() = _listTask

    private val _logoutTriggerS = MutableLiveData<Boolean>()
    val logoutTriggerS: LiveData<Boolean>
        get() = _logoutTriggerS


    init {
//        getListCategory()
    }

     fun getListTask() {
        _listTask.postValue(ResponseState.Start)
        viewModelScope.launch(Dispatchers.IO) {
            remoteRepo.getListTask { list, state ->
                if (state) {
                    val task = mutableListOf<Task>()
//                    val group = mutableListOf<Task>()

                    list.forEach {
                        if (it.typeTask == TypeTask.PERSONAL) {
                            if (Pref.userId == it.userId) {
                                task.add(it)
                            }
                        } else {
                            it.listMember.forEach { u ->
                                if (u.uid == Pref.userId) {
                                    task.add(it)
                                }
                            }
                        }
                    }

                    _listTask.postValue(ResponseState.Success(task))
                } else {
                    _listTask.postValue(
                        ResponseState.Failure(Throwable("Không tìm thấy dữ liệu. Thử lại sau !!"))
                    )
                }
            }
        }
    }

    private fun getListCategory() = viewModelScope.launch(Dispatchers.IO) {
        remoteRepo.getListCategory() { list, state ->
            if (state) {
                _listCategories.postValue(list)
            } else {
                _listCategories.postValue(emptyList())
            }
        }
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun logoutCurrentUser() {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _logoutTriggerS.value = user == null
        }

        auth.addAuthStateListener(authStateListener)
        auth.signOut()

// mAuth.removeAuthStateListener(authStateListener)
    }
}