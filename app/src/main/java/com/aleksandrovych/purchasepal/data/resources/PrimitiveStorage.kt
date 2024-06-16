package com.aleksandrovych.purchasepal.data.resources

import kotlin.reflect.KClass

interface PrimitiveStorage {

    suspend fun <T : Any> getValue(key: String, targetType: KClass<T>): T?

    suspend fun transaction(transaction: Editor.() -> Unit)

    interface Editor {

        fun <T : Any> putValue(key: String, value: T)

        fun remove(key: String)
    }
}