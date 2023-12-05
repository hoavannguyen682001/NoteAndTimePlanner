package com.hoanv.notetimeplanner.data.repository.remote

import com.hoanv.notetimeplanner.data.models.Category

interface RemoteRepo {
    fun addNewCategory(category: Category, result: (Boolean) -> Unit)

    fun getListCategory(result: (List<Category>, Boolean) -> Unit)

    fun deleteCategory(category: Category, result: (Boolean) -> Unit)

    fun updateCategory(category: Category, result: (Boolean) -> Unit)
}