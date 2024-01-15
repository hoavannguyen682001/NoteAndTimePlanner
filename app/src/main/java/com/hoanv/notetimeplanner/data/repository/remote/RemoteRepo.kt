package com.hoanv.notetimeplanner.data.repository.remote

import com.google.firebase.auth.FirebaseUser
import android.net.Uri
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.FileInfo
import com.hoanv.notetimeplanner.data.models.ImageInfo
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.data.models.group.GroupNotification
import com.hoanv.notetimeplanner.data.models.group.ResponseKey
import com.hoanv.notetimeplanner.data.models.notification.NotificationData
import com.hoanv.notetimeplanner.data.models.notification.ResponseNoti
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface RemoteRepo {
    /**
     * Authentication
     */
    fun registerUserAccount(userInfo: UserInfo, result: (Boolean) -> Unit)
    fun signInUserAccount(userInfo: UserInfo, result: (Boolean) -> Unit)
    fun signInWithGoogle(idToken: String, result: (FirebaseUser?, Boolean) -> Unit)
    fun createUserInfoByGoogleAuth(userInfo: UserInfo)
    fun getUserInfo(email: String, result: (UserInfo?) -> Unit)
    fun updateUserInfo(userInfo: UserInfo, result: (Boolean) -> Unit)
    fun uploadAvatar(userId: String, imageUri: Uri, result: (String) -> Unit)


    /**
     * CURD Category
     * */
    fun addNewCategory(category: Category, result: (Boolean) -> Unit)
    fun getListCategory(result: (List<Category>, Boolean) -> Unit)
    fun deleteCategory(category: Category, result: (Boolean) -> Unit)
    fun updateCategory(category: Category, field: String, result: (Boolean) -> Unit)

    /**
     * CRUD Task
     * */
    fun addNewTask(task: Task, result: (Boolean) -> Unit)
    fun getListTask(result: (List<Task>, Boolean) -> Unit)
    fun deleteTask(task: Task, result: (Boolean) -> Unit)
    fun updateTask(task: Task, result: (Boolean) -> Unit)
    fun getDetailTask(taskId: String, result: (Task?) -> Unit)

    fun uploadImageOfTask(task: Task, listUri: List<ImageInfo>, result: (Task, Boolean) -> Unit)

    fun uploadFileOfTask(task: Task, listUri: List<FileInfo>, result: (Task, Boolean) -> Unit)

    /**
     * Get List Task by Category
     */
    fun getListTaskByCategory(category: Category, result: (List<Task>, Boolean) -> Unit)

    fun getIconCategories(result: (List<String>) -> Unit)

    /**
     * Get access token of service-account.json
     */
    fun getAccessToken(scope: MutableList<String>, path: InputStream): Flow<String>

    /**
     * Get device token
     */
    fun getDeviceToken(result: (String) -> Unit)

    /**
     * Send notification
     */
    fun sendNotification(body: NotificationData): Flow<ResponseNoti>

    fun createGroupNotification(body: GroupNotification): Flow<ResponseKey>
}