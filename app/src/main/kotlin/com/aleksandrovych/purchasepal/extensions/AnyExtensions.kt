package com.aleksandrovych.purchasepal.extensions

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

val Any.loggingTag get() = this::class.simpleName.toString()

val Any.coroutineExceptionLogger: CoroutineExceptionHandler
    get() {
        return CoroutineExceptionHandler { _, throwable ->
            Log.w(loggingTag, "Unhandled coroutine exception", throwable)
        }
    }