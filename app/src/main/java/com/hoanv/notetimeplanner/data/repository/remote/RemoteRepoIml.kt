package com.hoanv.notetimeplanner.data.repository.remote

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnCompleteListener
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.FileInfo
import com.hoanv.notetimeplanner.data.models.ImageInfo
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.data.models.group.GroupNotification
import com.hoanv.notetimeplanner.data.models.group.ResponseKey
import com.hoanv.notetimeplanner.data.models.notification.NotificationData
import com.hoanv.notetimeplanner.data.models.notification.ResponseNoti
import com.hoanv.notetimeplanner.data.remote.AppApi
import com.hoanv.notetimeplanner.utils.AppConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.InputStream

class RemoteRepoIml(
    private val fireStore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val appApi: AppApi,
    private val firebaseStorage: FirebaseStorage
) : RemoteRepo {
    /**
     * Authentication
     */
    override fun registerUserAccount(userInfo: UserInfo, result: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(userInfo.userEmail, userInfo.userPassword)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    result.invoke(true)
                } else {
                    result.invoke(false)
                    Log.d("CREATE_USER_ACCOUNT", "${it.exception}")
                }
            }
    }

    override fun createUserInfoByGoogleAuth(userInfo: UserInfo) {
        fireStore.collection(AppConstant.USER_TBL_NAME)
            .document(userInfo.uid)
            .set(userInfo.hashMap())
            .addOnSuccessListener {
                Log.d("createUserInfoByGoogleAuth", "Success")
            }
            .addOnFailureListener {
                Log.d("createUserInfoByGoogleAuth", "${it.message}")
            }
    }

    override fun signInUserAccount(userInfo: UserInfo, result: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(userInfo.userEmail, userInfo.userPassword)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    result.invoke(true)
                } else {
                    result.invoke(false)
                    Log.d("SIGN_IN_USER_ACCOUNT", "${it.exception}")
                }
            }
    }

    override fun signInWithGoogle(idToken: String, result: (FirebaseUser?, Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val taskResult = it.result
                    val firebaseUser = taskResult.user
                    Log.d("SIGN_IN_USER_ACCOUNT", "${taskResult.user}")
                    result.invoke(firebaseUser, true)
                } else {
                    result.invoke(null, false)
                    Log.d("SIGN_IN_USER_ACCOUNT", "${it.exception}")
                }
            }
    }

    override fun getUserInfo(email: String, result: (UserInfo?) -> Unit) {
        fireStore.collection(AppConstant.USER_TBL_NAME)
            .whereEqualTo("userEmail", email)
            .get()
            .addOnSuccessListener {
                var task: UserInfo? = null
                for (doc in it) {
                    task = doc.toObject(UserInfo::class.java)
                    Log.d("GET_USER_INFO1", "${task}")
                }
                result.invoke(task)
            }.addOnFailureListener {
                result.invoke(null)
                Log.d("GET_USER_INFO", "${it.message}")
            }
    }

    override fun uploadAvatar(userId: String, imageUri: Uri, result: (String) -> Unit) {
        val imageRef = firebaseStorage.reference.child("images/$userId")

        // Upload the file and metadata
        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Image uploaded successfully
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    result.invoke(imageUrl)
                }
            } else {
                Log.d("uploadAvatar", "${task.exception}")
            }
        }
    }

    override fun updateUserInfo(userInfo: UserInfo, result: (Boolean) -> Unit) {
        fireStore.collection(AppConstant.USER_TBL_NAME)
            .document(userInfo.uid)
            .set(userInfo.hashMap(), SetOptions.merge())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    result.invoke(true)
                } else {
                    result.invoke(false)
                    Log.d("updateUserInfo", "${task.exception}")
                }
            }
    }

    /**
     * Category
     */
    override fun addNewCategory(category: Category, result: (Boolean) -> Unit) {
        fireStore.collection(AppConstant.CATEGORY_TBL_NAME).document(category.id)
            .set(category.hashMap()).addOnSuccessListener {
                result.invoke(true)
            }.addOnFailureListener {
                result.invoke(false)
                Log.d("ADD_NEW_CATE", "${it.message}")
            }
    }

    override fun getListCategory(result: (List<Category>, Boolean) -> Unit) {
        fireStore.collection(AppConstant.CATEGORY_TBL_NAME).get().addOnSuccessListener {
            val categories = mutableListOf<Category>()
            for (doc in it) {
                categories.add(doc.toObject(Category::class.java))
            }
            result.invoke(categories, true)
        }.addOnFailureListener {
            result.invoke(emptyList(), false)
        }
    }

    override fun deleteCategory(category: Category, result: (Boolean) -> Unit) {
        fireStore.collection(AppConstant.CATEGORY_TBL_NAME).document(category.id).delete()
            .addOnSuccessListener {
                result.invoke(true)
            }.addOnFailureListener {
                result.invoke(false)
                Log.d("DELETE_CATE", "${it.message}")
            }
    }

    override fun updateCategory(category: Category, field: String, result: (Boolean) -> Unit) {
        fireStore.collection(AppConstant.CATEGORY_TBL_NAME)
            .document(category.id)
            .set(category.hashMap(), SetOptions.merge())
            .addOnSuccessListener {
                result.invoke(true)
            }.addOnFailureListener {
                result.invoke(false)
                Log.d("UPDATE_CATE", "${it.message}")
            }
    }

    /**
     * Task
     */
    override fun addNewTask(task: Task, result: (Boolean) -> Unit) {
        fireStore.collection(AppConstant.TASK_TBL_NAME).document(task.id).set(task.hashMap())
            .addOnSuccessListener {
                result.invoke(true)
            }.addOnFailureListener {
                result.invoke(false)
                Log.d("ADD_NEW_TASK", "${it.message}")
            }
    }

    override fun uploadImageOfTask(
        task: Task,
        listUri: List<ImageInfo>,
        result: (Task, Boolean) -> Unit
    ) {
        listUri.forEachIndexed { index, image ->
            val imageRef =
                firebaseStorage.reference.child(
                    "tasks/${task.id}/images/${image.idImage}"
                )

            // Upload the file and metadata
            val uploadTask = image.imageUrl?.let { imageRef.putFile(it.toUri()) }
            uploadTask?.addOnCompleteListener { mTask ->
                if (mTask.isSuccessful) {
                    // Image uploaded successfully
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        //map download url
                        task.attachFile.listImage.map {
                            if (it.idImage == image.idImage) {
                                it.imageUrl = downloadUri.toString()
                            }
                        }
                        if ((index + 1) == task.attachFile.listImage.size) {
                            result.invoke(task, true)
                        }
                    }
                } else {
                    result.invoke(task, false)
                    Log.d("uploadImageOfTask", "${mTask.exception}")
                }
            }
        }
    }

    override fun uploadFileOfTask(
        task: Task,
        listUri: List<FileInfo>,
        result: (Task, Boolean) -> Unit
    ) {
        listUri.forEachIndexed { index, item ->
            val imageRef =
                firebaseStorage.reference.child(
                    "tasks/${task.id}/files/${item.idFile}-${item.title}"
                )

            // Upload the file and metadata
            val uploadTask = item.fileUrl?.let { imageRef.putFile(it.toUri()) }
            uploadTask?.addOnCompleteListener { mTask ->
                if (mTask.isSuccessful) {
                    // Image uploaded successfully
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        //map download url
                        task.attachFile.listFile.map {
                            if (it.idFile == item.idFile) {
                                it.fileUrl = downloadUri.toString()
                            }
                        }
                        if ((index + 1) == task.attachFile.listFile.size) {
                            result.invoke(task, true)
                        }
                    }
                } else {
                    result.invoke(task, false)
                    Log.d("uploadImageOfTask", "${mTask.exception}")
                }
            }
        }
    }

    override fun getListTask(result: (List<Task>, Boolean) -> Unit) {
        val task = mutableListOf<Task>()
        fireStore.collection(AppConstant.TASK_TBL_NAME)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot) {
                    task.add(doc.toObject(Task::class.java))
                }
                result.invoke(task, true)
            }
            .addOnFailureListener {
                result.invoke(emptyList(), false)
                Log.d("GET_TASK", "${it.message}")
            }
    }

    override fun deleteTask(task: Task, result: (Boolean) -> Unit) {
        fireStore.collection(AppConstant.TASK_TBL_NAME).document(task.id)
            .delete()
            .addOnSuccessListener {
                result.invoke(true)
            }.addOnFailureListener {
                result.invoke(false)
                Log.d("UPDATE_CATE", "${it.message}")
            }
    }

    override fun updateTask(task: Task, result: (Boolean) -> Unit) {
        fireStore.collection(AppConstant.TASK_TBL_NAME).document(task.id)
            .set(task.hashMap(), SetOptions.merge())// Merge object
            .addOnSuccessListener {
                result.invoke(true)
            }.addOnFailureListener {
                result.invoke(false)
                Log.d("UPDATE_CATE", "${it.message}")
            }
    }

    override fun getDetailTask(taskId: String, result: (Task?) -> Unit) {
        fireStore.collection(AppConstant.TASK_TBL_NAME).document(taskId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    result.invoke(null)
                    Log.d("GET_DETAIL_TASK", "${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    result.invoke(snapshot.toObject(Task::class.java)!!)
                } else {
                    result.invoke(null)
                }
            }
    }

    /**
     * Task by Category
     */
    override fun getListTaskByCategory(category: Category, result: (List<Task>, Boolean) -> Unit) {
        fireStore.collection(AppConstant.TASK_TBL_NAME)
            .whereEqualTo("category.title", category.title)
            .get()
            .addOnSuccessListener {
                val task = mutableListOf<Task>()
                for (doc in it) {
                    task.add(doc.toObject(Task::class.java))
                }
                result.invoke(task, true)
            }.addOnFailureListener {
                result.invoke(emptyList(), false)
                Log.d("GET_TASK", "${it.message}")
            }
    }

    /**
     * Get icons category
     */
    override fun getIconCategories(result: (List<String>) -> Unit) {
        firebaseStorage.reference.child("icons")
            .listAll()
            .addOnSuccessListener { list ->
                val listUri = mutableListOf<String>()
                for (item in list.items) {
                    item.downloadUrl
                        .addOnSuccessListener {
                            listUri.add(it.toString())
                            if (listUri.size == list.items.size) {
                                result.invoke(listUri)
                            }
                        }
                        .addOnFailureListener {
                            Log.d("GetIconCategories", "${it.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.d("GetIconCategories", "${e.message}")
            }
    }

    /**
     * Token
     */
    override fun getAccessToken(scope: MutableList<String>, path: InputStream): Flow<String> =
        flow {
            val googleCredentials = GoogleCredentials.fromStream(path).createScoped(scope)
            googleCredentials.refresh()
            emit(googleCredentials.refreshAccessToken().tokenValue)
        }.flowOn(Dispatchers.IO)

    override fun getDeviceToken(result: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("TAGGGGGGGG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            result.invoke(task.result)
        })
    }

    /**
     * Send notification
     */
    override fun sendNotification(body: NotificationData): Flow<ResponseNoti> = flow {
        emit(appApi.sendNotification(body))
    }.flowOn(Dispatchers.IO)

    override fun createGroupNotification(body: GroupNotification): Flow<ResponseKey> = flow {
        emit(appApi.createGroupNotification(body))
    }.flowOn(Dispatchers.IO)
}