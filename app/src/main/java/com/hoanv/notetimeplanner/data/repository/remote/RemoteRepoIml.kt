package com.hoanv.notetimeplanner.data.repository.remote

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.hoanv.notetimeplanner.data.models.Category
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
                    fireStore.collection(AppConstant.USER_TBL_NAME)
                        .document(userInfo.uid)
                        .set(userInfo.hashMap())

                    result.invoke(true)
                } else {
                    result.invoke(false)
                    Log.d("CREATE_USER_ACCOUNT", "${it.exception}")
                }
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

    override fun signInWithGoogle(idToken: String, result: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    result.invoke(true)
                } else {
                    result.invoke(false)
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

                Log.d("GET_USER_INFO2", "${task}")
            }.addOnFailureListener {
                result.invoke(null)
                Log.d("GET_USER_INFO", "${it.message}")
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

    override fun getListTask(result: (List<Task>, Boolean) -> Unit) {
        val task = mutableListOf<Task>()
        fireStore.collection(AppConstant.TASK_TBL_NAME)
            .get()
            .addOnSuccessListener {
                for (doc in it) {
                    task.add(doc.toObject(Task::class.java))
                }
                result.invoke(task, true)
            }.addOnFailureListener {
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
            .get()
            .addOnSuccessListener {
                it?.let {
                    result.invoke(it.toObject(Task::class.java)!!)
                }
            }
            .addOnFailureListener {
                result.invoke(null)
                Log.d("GET_DETAIL_TASK", "${it.message}")
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