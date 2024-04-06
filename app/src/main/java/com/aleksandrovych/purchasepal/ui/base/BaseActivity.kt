package com.aleksandrovych.purchasepal.ui.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.aleksandrovych.purchasepal.extensions.inflateBinding
import com.aleksandrovych.purchasepal.extensions.lifecycle

abstract class BaseActivity<VB : ViewBinding> : InjectableActivity(), BaseBindingComponent<VB> {

    @Suppress("MemberVisibilityCanBePrivate")
    protected val binding: VB? by lifecycle { inflateBinding(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding!!.root)
    }
}