package com.aleksandrovych.purchasepal.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.aleksandrovych.purchasepal.extensions.inflateBinding

abstract class BaseFragment<VB : ViewBinding> : InjectableFragment(), BaseBindingComponent<VB> {

    protected var binding: VB? = null

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = inflateBinding(inflater, container)
        this.binding = binding
        onBindingCreated(binding)
        return binding.root
    }

    @CallSuper
    final override fun onDestroyView() {
        binding?.let(::onDestroyBinding)
        binding = null
        super.onDestroyView()
    }

    protected open fun onBindingCreated(binding: VB) {
    }

    protected open fun onDestroyBinding(binding: VB) {
    }
}