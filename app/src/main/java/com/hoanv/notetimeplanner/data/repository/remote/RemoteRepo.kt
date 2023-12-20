package com.hoanv.notetimeplanner.data.repository.remote

import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.data.models.Task

interface RemoteRepo {

    /*
    * CURD Category
    * */
    fun addNewCategory(category: Category, result: (Boolean) -> Unit)
    fun getListCategory(result: (List<Category>, Boolean) -> Unit)
    fun deleteCategory(category: Category, result: (Boolean) -> Unit)
    fun updateCategory(category: Category, field: String, result: (Boolean) -> Unit)

    /*
    * CRUD Task
    * */
    fun addNewTask(task: Task, result: (Boolean) -> Unit)
    fun getListTask(result: (List<Task>, Boolean) -> Unit)
    fun deleteTask(task: Task, result: (Boolean) -> Unit)
    fun updateTask(task: Task, result: (Boolean) -> Unit)

    /**
     * Get List Task by Category
     */
    fun getListTaskByCategory(category: Category, result: (List<Task>, Boolean) -> Unit)
}