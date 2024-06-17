package com.aleksandrovych.purchasepal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.aleksandrovych.purchasepal.databinding.ActivityMainBinding
import com.aleksandrovych.purchasepal.extensions.launchWhenCreated
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsFragmentDirections
import com.aleksandrovych.purchasepal.ui.base.BaseActivity
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuyFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()
    private val navController: NavController?
        get() = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)?.findNavController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkDeepLinks(intent)

        launchWhenCreated {
            viewModel.listAlreadySharedEventFlow.collect {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.message_list_already_added),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        launchWhenCreated {
            viewModel.badListEventFlow.collect {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.message_list_damaged_or_removed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        launchWhenCreated {
            viewModel.lastViewedListFlow.collect { list ->
                restoreLastViewedList(list)
            }
        }

        viewModel.restoreLastViewedList()
        observeNavigationDestinationChanges()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.checkDeepLinks(intent)
    }

    private fun observeNavigationDestinationChanges() {
        lifecycleScope.launch(Dispatchers.Default) {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                callbackFlow<Unit> {
                    val listener: (NavController, NavDestination, Bundle?) -> Unit =
                        { _, destination, args ->
                            if (destination.id == R.id.whatToBuyFragment && args != null) {
                                val listArg = WhatToBuyFragmentArgs
                                    .fromBundle(args)
                                    .whatToBuyListArg
                                viewModel.saveLastViewedList(listArg)
                            } else if (destination.id == R.id.whatToBuyListsFragment) {
                                viewModel.removeLastViewedList()
                            }
                        }
                    navController?.addOnDestinationChangedListener(listener)
                    awaitClose { navController?.removeOnDestinationChangedListener(listener) }
                }.collect()
            }
        }
    }

    private suspend fun restoreLastViewedList(listArg: WhatToBuyList) {
        if (navController?.currentDestination?.id != R.id.whatToBuyListsFragment) return
        withContext(Dispatchers.Main) {
            navController
                ?.navigate(WhatToBuyListsFragmentDirections.actionWhatToBuyListsFragmentToWhatToBuyFragment(listArg))
        }
    }
}