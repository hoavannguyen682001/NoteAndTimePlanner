package com.hoanv.notetimeplanner.data.remote

import com.hoanv.notetimeplanner.data.models.Task
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.POST

interface AppApi {
    @POST
    fun sendNotification(@Body body: Task): Flow<Unit>

}