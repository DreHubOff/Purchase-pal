package com.aleksandrovych.purchasepal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.aleksandrovych.purchasepal.databinding.ActivityMainBinding
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsFragmentDirections
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuyFragmentArgs
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val LAST_VIEWED_LIST_KEY = "LAST_VIEWED_LIST"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var binding: ActivityMainBinding? = null
    private val navController: NavController?
        get() {
            return (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)
                ?.findNavController()
        }
    private val preferences by lazy { getSharedPreferences("prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        viewModel.checkDeepLinks(intent)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.listAlreadySharedEventFlow.collect {
                    Toast.makeText(
                        this@MainActivity,
                        "Этот список уже добавлен",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.badListEventFlow.collect {
                    Toast.makeText(
                        this@MainActivity,
                        "Этот список был поврежден или удален",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        restoreLastViewedList()
        observeLastViewedList()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.checkDeepLinks(intent)
    }

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
                    WhatToBuyListsFragmentDirections.actionWhatToBuyListsFragmentToWhatToBuyFragment(
                        listArg
                    )
                )
            }
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}