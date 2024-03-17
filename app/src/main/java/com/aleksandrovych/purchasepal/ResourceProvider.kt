package com.aleksandrovych.purchasepal

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

class ResourceProvider private constructor(private val resources: Resources) {

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(resourcePointer: ResourcePointer<T>): T {
        return when (resourcePointer) {
            is ResourcePointer.StringPointer -> resources.getString(resourcePointer.id, *resourcePointer.args) as T
        }
    }

    sealed interface ResourcePointer<T> {

        class StringPointer(@StringRes val id: Int, val args: Array<out Any?>) : ResourcePointer<String>
    }

    companion object {

        fun Int.toStringPointer(vararg args: Any?) = ResourcePointer.StringPointer(this, args)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object ResourceProviderModule {

        @Provides
        fun getResourceProvider(@ApplicationContext context: Context): ResourceProvider =
            ResourceProvider(context.resources)
    }
}