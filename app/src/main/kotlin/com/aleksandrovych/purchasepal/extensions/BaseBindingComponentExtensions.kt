package com.aleksandrovych.purchasepal.extensions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.aleksandrovych.purchasepal.ui.base.BaseBindingComponent
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

private const val BINDING_GENERIC_POSITION = 0

fun <VB : ViewBinding> BaseBindingComponent<VB>.inflateBinding(layoutInflater: LayoutInflater): VB {
    val bindingType = findGenericAt(BINDING_GENERIC_POSITION)
    val inflateMethod = (bindingType as Class<*>).findMethod(
        parameterTypes = arrayOf(LayoutInflater::class.java),
        returnType = bindingType,
    )
    @Suppress("UNCHECKED_CAST")
    return inflateMethod.invoke(null, layoutInflater) as VB
}

fun <VB : ViewBinding> BaseBindingComponent<VB>.inflateBinding(
    layoutInflater: LayoutInflater,
    container: ViewGroup?,
    attachToParent: Boolean = false,
): VB {
    val bindingType = findGenericAt(BINDING_GENERIC_POSITION)
    val inflateMethod = (bindingType as Class<*>).findMethod(
        parameterTypes = arrayOf(LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java),
        returnType = bindingType,
    )
    @Suppress("UNCHECKED_CAST")
    return inflateMethod.invoke(null, layoutInflater, container, attachToParent) as VB
}

private fun Any.findGenericAt(position: Int): Type =
    (this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[position]

private fun Class<*>.findMethod(vararg parameterTypes: Class<*>, returnType: Class<*>): Method {
    return methods.first { method ->
        method.parameterCount == parameterTypes.size
                && method.parameters.all { parameterTypes.contains(it.type) }
                && method.returnType == returnType
    }
}