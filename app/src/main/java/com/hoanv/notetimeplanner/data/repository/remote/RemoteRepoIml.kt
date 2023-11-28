package com.hoanv.notetimeplanner.data.repository.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.utils.CommonConstant

class RemoteRepoIml(
    private val fireStore: FirebaseFirestore
) : RemoteRepo {
    override fun addNewCategory(category: Category, result: (Boolean) -> Unit) {
        fireStore.collection(CommonConstant.CATEGORY_TBL_NAME)
            .add(category.hashMap())
            .addOnSuccessListener {
                result.invoke(true)
            }
            .addOnFailureListener {
                result.invoke(false)
                Log.d("ADD_NEW_CATE", "${it.message}")
            }
    }

    override fun getListCategory(result: (List<Category>) -> Unit) {
        fireStore.collection(CommonConstant.CATEGORY_TBL_NAME)
            .get()
            .addOnSuccessListener {
                val categories = mutableListOf<Category>()
                for (doc in it) {
                    categories.add(doc.toObject(Category::class.java))
                    Log.d("###", "${categories.size}")
                }
                categories.forEach {
                    Log.d("###", "${it}")
                }
                result.invoke(categories)
            }
            .addOnFailureListener {
                result.invoke(emptyList())
                Log.d("GET_CATE", "${it.message}")
            }
    }

    override fun deleteCategory(category: Category, result: (Boolean) -> Unit) {

    }
}