package com.aleksandrovych.purchasepal.data.resources.local

import android.content.SharedPreferences
import com.aleksandrovych.purchasepal.data.resources.PrimitiveStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

class PrimitiveStorageImpl(
    private val sharedPreferences: SharedPreferences,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PrimitiveStorage {

    override suspend fun <T : Any> getValue(key: String, targetType: KClass<T>): T? = withContext(ioDispatcher) {
        if (!sharedPreferences.contains(key)) return@withContext null
        @Suppress("UNCHECKED_CAST")
        when (targetType) {
            String::class -> sharedPreferences.getString(key, null)
            Boolean::class -> sharedPreferences.getBoolean(key, false)
            Long::class -> sharedPreferences.getLong(key, 0L)
            Int::class -> sharedPreferences.getInt(key, 0)
            else -> throw UnsupportedOperationException("${targetType.simpleName} is not supported")
        } as T
    }

    override suspend fun transaction(transaction: PrimitiveStorage.Editor.() -> Unit) = withContext(ioDispatcher) {
        EditorImpl(sharedPreferences.edit()).run {
            transaction()
            commit()
        }
    }

    private class EditorImpl(private val preferencesEditor: SharedPreferences.Editor) : PrimitiveStorage.Editor {

        override fun <T : Any> putValue(key: String, value: T) {
            when (value::class) {
                String::class -> preferencesEditor.putString(key, value as String)
                Boolean::class -> preferencesEditor.putBoolean(key, value as Boolean)
                Long::class -> preferencesEditor.putLong(key, value as Long)
                Int::class -> preferencesEditor.putInt(key, value as Int)
                else -> throw UnsupportedOperationException("${value::class.simpleName} is not supported")
            }
        }

        override fun remove(key: String) {
            preferencesEditor.remove(key)
        }

        fun commit() {
            preferencesEditor.commit()
        }
    }
}