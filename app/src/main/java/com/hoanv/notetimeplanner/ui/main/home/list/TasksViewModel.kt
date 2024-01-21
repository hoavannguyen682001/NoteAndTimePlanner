package com.hoanv.notetimeplanner.ui.main.home.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.TypeTask
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val remoteRepo: RemoteRepo
) : BaseViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    private val _listCategory =
        MutableLiveData<ResponseState<List<Category>>>()
    val listCategory: LiveData<ResponseState<List<Category>>>
        get() = _listCategory

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

    private val _userInfo = MutableLiveData<ResponseState<UserInfo>>()
    val userInfo: LiveData<ResponseState<UserInfo>>
        get() = _userInfo

    init {
        if (currentUser != null) {
            getUserInfo()
        }
    }

    fun getUserInfo() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val email = currentUser!!.email!!
            _userInfo.postValue(ResponseState.Start)
            remoteRepo.getUserInfo(email) { userInfo ->
                if (userInfo != null) {
                    Pref.userId = userInfo.uid
                    Pref.userEmail = userInfo.userEmail

                    if (Pref.deviceToken != userInfo.userToken) {
                        if (Pref.deviceToken.isNotEmpty() && Pref.deviceToken != "") {
                            /* Update user token when token change or login in other devices */
                            Log.d("USER_TOKEN", "${Pref.deviceToken} - ${userInfo.userToken}")

                            userInfo.userToken = Pref.deviceToken
                            remoteRepo.updateUserInfo(userInfo) {}
                        }
                    }

                    getListTask(userInfo)

                    Log.d("USER_ID", Pref.userId)
                    _userInfo.postValue(ResponseState.Success(userInfo))
                } else {
                    _userInfo.postValue(ResponseState.Failure(Throwable("Fail")))
                }
            }
        } catch (e: Exception) {
            _userInfo.postValue(ResponseState.Failure(Throwable("Có lỗi không xác định. Thử lại sau!")))
            Log.d("GetUserInfo", "${e.message}")
        }
    }


    fun getListCategory() {
        _listCategory.postValue(ResponseState.Start)
        viewModelScope.launch(Dispatchers.IO) {
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


    fun getListTask(userInfo: UserInfo) {
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

    fun getListTaskByCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            _listTaskPersonal.postValue(ResponseState.Start)
            remoteRepo.getListTaskByCategory(category) { list, state ->
                if (state) {
                    _listTaskPersonal.postValue(ResponseState.Success(list.toMutableList()))
                } else {
                    _listTaskPersonal.postValue(
                        ResponseState.Failure(Throwable("Không tìm thấy dữ liệu. Thử lại sau !!"))
                    )
                }
            }
        }
    }
}