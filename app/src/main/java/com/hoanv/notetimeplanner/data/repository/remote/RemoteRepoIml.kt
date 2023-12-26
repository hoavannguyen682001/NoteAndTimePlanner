package com.hoanv.notetimeplanner.data.repository.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.utils.AppConstant

class RemoteRepoIml(
    private val fireStore: FirebaseFirestore,
    private val auth: FirebaseAuth
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
            .set(category.hashMap(), SetOptions.mergeFields(field))
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

}