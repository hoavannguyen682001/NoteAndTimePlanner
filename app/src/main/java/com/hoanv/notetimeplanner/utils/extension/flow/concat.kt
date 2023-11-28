package com.hoanv.notetimeplanner.utils.extension.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> concat(first: Flow<T>, second: Flow<T>): Flow<T> {
    return flow {
        first.collect { emit(it) }
        second.collect { emit(it) }
    }
}