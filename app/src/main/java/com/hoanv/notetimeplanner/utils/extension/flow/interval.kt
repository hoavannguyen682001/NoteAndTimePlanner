package com.hoanv.notetimeplanner.utils.extension.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

fun interval(
    initialDelay: Long = 0L,
    delay: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    initialCount: Long = 0L
): Flow<Long> {
    require(initialDelay >= 0) { "Expected non-negative delay, but has $initialDelay ms" }
    require(delay >= 0) { "Expected non-negative delay, but has $delay ms" }
    return flow {
        delay(unit.toMillis(initialDelay))

        var count = initialCount
        while (true) {
            emit(count++)
            delay(unit.toMillis(delay))
        }
    }
}
