package com.hoanv.notetimeplanner.data.repository.remote

import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Todo

interface RemoteRepo {

    /*
    * CURD Category
    * */
    fun addNewCategory(category: Category, result: (Boolean) -> Unit)
    fun getListCategory(result: (List<Category>, Boolean) -> Unit)
    fun deleteCategory(category: Category, result: (Boolean) -> Unit)
    fun updateCategory(category: Category, result: (Boolean) -> Unit)

    /*
    * CRUD Task
    * */
    fun addNewTask(todo: Todo, result: (Boolean) -> Unit)
    fun getListTask(result: (List<Todo>, Boolean) -> Unit)
    fun deleteTask(todo: Todo, result: (Boolean) -> Unit)
    fun updateTask(todo: Todo, result: (Boolean) -> Unit)
}