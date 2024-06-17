package com.aleksandrovych.purchasepal.data.resources.local

import android.content.res.Resources
import androidx.annotation.StringRes
import javax.inject.Inject

class ResourceProvider @Inject constructor(private val resources: Resources) {

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
}