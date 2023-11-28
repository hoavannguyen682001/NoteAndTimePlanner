package com.hoanv.notetimeplanner.data.repository.remote

import com.hoanv.notetimeplanner.data.models.Category

interface RemoteRepo {
    fun addNewCategory(category: Category, result: (Boolean) -> Unit)

    fun getListCategory(result: (List<Category>) -> Unit)

    fun deleteCategory(category: Category, result: (Boolean) -> Unit)
}