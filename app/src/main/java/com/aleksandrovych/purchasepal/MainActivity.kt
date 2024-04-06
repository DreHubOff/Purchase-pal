package com.aleksandrovych.purchasepal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.edit
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
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val LAST_VIEWED_LIST_KEY = "LAST_VIEWED_LIST"
private const val LAST_VIEWED_STORAGE_FILE_NAME = "last-viewed-storage"

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()
    private val navController: NavController?
        get() = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)?.findNavController()
    private val preferences by lazy { getSharedPreferences(LAST_VIEWED_STORAGE_FILE_NAME, MODE_PRIVATE) }

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

        restoreLastViewedList()
        observeLastViewedList()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.checkDeepLinks(intent)
    }

    // TODO: Move to data layer. Track progress here: https://github.com/DreHubOff/Purchase-pal/issues/1
    private fun observeLastViewedList() {
        lifecycleScope.launch(Dispatchers.Default) {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                callbackFlow<Unit> {
                    val listener: (NavController, NavDestination, Bundle?) -> Unit =
                        { _, destination, args ->
                            preferences.edit {
                                if (destination.id == R.id.whatToBuyFragment && args != null) {
                                    val listArg = WhatToBuyFragmentArgs
                                        .fromBundle(args)
                                        .whatToBuyListArg
                                    putString(LAST_VIEWED_LIST_KEY, Gson().toJson(listArg))
                                } else if (destination.id == R.id.whatToBuyListsFragment) {
                                    remove(LAST_VIEWED_LIST_KEY)
                                }
                            }
                        }
                    navController?.addOnDestinationChangedListener(listener)
                    awaitClose { navController?.removeOnDestinationChangedListener(listener) }
                }.collect()
            }
        }
    }

    private fun restoreLastViewedList() {
        lifecycleScope.launch(Dispatchers.Default) {
            if (navController?.currentDestination?.id != R.id.whatToBuyListsFragment) return@launch
            val args = preferences.getString(LAST_VIEWED_LIST_KEY, null) ?: return@launch
            val listArg = Gson().fromJson(args, WhatToBuyList::class.java)
            withContext(Dispatchers.Main) {
                navController?.navigate(
                    WhatToBuyListsFragmentDirections.actionWhatToBuyListsFragmentToWhatToBuyFragment(listArg)
                )
            }
        }
    }
}