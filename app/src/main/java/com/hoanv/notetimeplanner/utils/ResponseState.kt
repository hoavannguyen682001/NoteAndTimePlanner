package com.hoanv.notetimeplanner.utils
sealed class ResponseState<out T> {
    object Start : ResponseState<Nothing>()
    data class Failure(val throwable: Throwable?) : ResponseState<Nothing>()
    data class Success<T>(val data: T) : ResponseState<T>()
}