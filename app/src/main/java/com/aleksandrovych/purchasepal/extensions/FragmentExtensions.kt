package com.aleksandrovych.purchasepal.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun Fragment.launchWhenResumed(
    context: CoroutineContext = coroutineExceptionLogger,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
): Job = launchOnLifecycle(Lifecycle.State.RESUMED, context, start, block)

private fun Fragment.launchOnLifecycle(
    state: Lifecycle.State,
    context: CoroutineContext,
    start: CoroutineStart,
    block: suspend CoroutineScope.() -> Unit,
): Job = lifecycleScope.launch(context, start) {
    viewLifecycleOwner.repeatOnLifecycle(state, block)
}