package com.hoanv.notetimeplanner.data.repository.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Todo
import com.hoanv.notetimeplanner.utils.CommonConstant

class RemoteRepoIml(
    private val fireStore: FirebaseFirestore
) : RemoteRepo {
    override fun addNewCategory(category: Category, result: (Boolean) -> Unit) {
        fireStore.collection(CommonConstant.CATEGORY_TBL_NAME)
            .document(category.id)
            .set(category.hashMap())
            .addOnSuccessListener {
                result.invoke(true)
            }
            .addOnFailureListener {
                result.invoke(false)
                Log.d("ADD_NEW_CATE", "${it.message}")
            }
    }

    override fun getListCategory(result: (List<Category>, Boolean) -> Unit) {
        fireStore.collection(CommonConstant.CATEGORY_TBL_NAME)
            .get()
            .addOnSuccessListener {
                val categories = mutableListOf<Category>()
                for (doc in it) {
                    categories.add(doc.toObject(Category::class.java))
                }
                result.invoke(categories, true)
            }
            .addOnFailureListener {
                result.invoke(emptyList(), false)
            }
    }

    override fun deleteCategory(category: Category, result: (Boolean) -> Unit) {
        fireStore.collection(CommonConstant.CATEGORY_TBL_NAME)
            .document(category.id)
            .delete()
            .addOnSuccessListener {
                result.invoke(true)
            }
            .addOnFailureListener {
                result.invoke(false)
                Log.d("DELETE_CATE", "${it.message}")
            }
    }

    override fun updateCategory(category: Category, result: (Boolean) -> Unit) {
        fireStore.collection(CommonConstant.CATEGORY_TBL_NAME)
            .document(category.id)
            .update("title", category.title)
            .addOnSuccessListener {
                result.invoke(true)
            }
            .addOnFailureListener {
                result.invoke(false)
                Log.d("UPDATE_CATE", "${it.message}")
            }
    }

    override fun addNewTask(todo: Todo, result: (Boolean) -> Unit) {
        fireStore.collection(CommonConstant.TASK_TBL_NAME)
            .document(todo.id)
            .set(todo.hashMap())
            .addOnSuccessListener {
                result.invoke(true)
            }
            .addOnFailureListener {
                result.invoke(false)
                Log.d("ADD_NEW_TASK", "${it.message}")
            }
    }

    override fun getListTask(result: (List<Todo>, Boolean) -> Unit) {
        fireStore.collection(CommonConstant.TASK_TBL_NAME)
            .get()
            .addOnSuccessListener {
                val task = mutableListOf<Todo>()
                for (doc in it) {
                    task.add(doc.toObject(Todo::class.java))
                }
                result.invoke(task, true)
            }
            .addOnFailureListener {
                result.invoke(emptyList(), false)
                Log.d("GET_TASK", "${it.message}")
            }
    }

    override fun deleteTask(todo: Todo, result: (Boolean) -> Unit) {
        fireStore.collection(CommonConstant.TASK_TBL_NAME)
            .document(todo.id)
            .delete()
            .addOnSuccessListener {
                result.invoke(true)
            }
            .addOnFailureListener {
                result.invoke(false)
                Log.d("UPDATE_CATE", "${it.message}")
            }
    }

    override fun updateTask(todo: Todo, result: (Boolean) -> Unit) {
        fireStore.collection(CommonConstant.TASK_TBL_NAME)
            .document(todo.id)
            .set(todo.hashMap(), SetOptions.merge())// Merge object
            .addOnSuccessListener {
                result.invoke(true)
            }
            .addOnFailureListener {
                result.invoke(false)
                Log.d("UPDATE_CATE", "${it.message}")
            }
    }
}