package com.aleksandrovych.purchasepal.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun <T> LifecycleOwner.lifecycle(
    releaseAction: ((T) -> Unit)? = null,
    initializer: () -> T,
): Lazy<T?> = LifecycleLazyImpl(initializer, releaseAction, this)

fun LifecycleOwner.launchWhenCreated(
    context: CoroutineContext = coroutineExceptionLogger,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
): Job = launchOnLifecycle(Lifecycle.State.CREATED, context, start, block)

fun LifecycleOwner.launchWhenResumed(
    context: CoroutineContext = coroutineExceptionLogger,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
): Job = launchOnLifecycle(Lifecycle.State.RESUMED, context, start, block)

private fun LifecycleOwner.launchOnLifecycle(
    state: Lifecycle.State,
    context: CoroutineContext,
    start: CoroutineStart,
    block: suspend CoroutineScope.() -> Unit,
): Job {
    val childLifecycleOwner = if (this is Fragment) viewLifecycleOwner else this
    return childLifecycleOwner.lifecycleScope.launch(context, start) {
        childLifecycleOwner.repeatOnLifecycle(state, block)
    }
}

private class LifecycleLazyImpl<out T>(
    private val initializer: () -> T,
    private val releaseAction: ((T) -> Unit)?,
    private val lifecycleOwner: LifecycleOwner,
) : Lazy<T?> {

    private var _value: T? = null

    override val value: T?
        get() {
            if (_value == null) {
                _value = initializer()
                lifecycleOwner.launchWhenCreated(coroutineExceptionLogger + Dispatchers.Default) {
                    try {
                        awaitCancellation()
                    } finally {
                        _value?.let { releaseAction?.invoke(it) }
                        _value = null
                    }
                }
            }
            return _value
        }

    override fun isInitialized(): Boolean = _value != null
}