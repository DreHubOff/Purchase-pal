package com.aleksandrovych.purchasepal

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.aleksandrovych.purchasepal.Cipher.encryptDecryptXOR
import com.aleksandrovych.purchasepal.data.repository.UserPreferencesRepository
import com.aleksandrovych.purchasepal.data.resources.remote.FirebaseDatabase.getShareListReference
import com.aleksandrovych.purchasepal.data.resources.remote.FirebaseDatabase.pullSharedList
import com.aleksandrovych.purchasepal.domain.DoAuthRequiredWorkInteractor
import com.aleksandrovych.purchasepal.domain.GetLastViewedListInteractor
import com.aleksandrovych.purchasepal.domain.SaveNewListInteractor
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.ui.base.BaseViewModel
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import com.aleksandrovych.purchasepal.whatToBuy.share.WhatToBuyShareOffline
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val whatToBuyListsDao: WhatToBuyListsDao,
    private val firebaseDb: FirebaseDatabase,
    private val saveNewListInteractor: SaveNewListInteractor,
    private val doAuthRequiredWorkInteractor: DoAuthRequiredWorkInteractor,
    private val getLastViewedListInteractor: GetLastViewedListInteractor,
    private val userPreferencesRepository: UserPreferencesRepository,
) : BaseViewModel() {

    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }

    val listAlreadySharedEventFlow = MutableSharedFlow<Unit>()
    val badListEventFlow = MutableSharedFlow<Unit>()
    val lastViewedListFlow = MutableSharedFlow<WhatToBuyList>()

    fun checkDeepLinks(intent: Intent?) {
        intent ?: return
        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.IO) {
            val link = Deeplink.getDynamicDeepLinks(intent) ?: return@launch
            saveDeepLinkList(link)
        }
    }

    fun saveLastViewedList(list: WhatToBuyList) {
        launch {
            userPreferencesRepository.saveLastViewedList(list)
        }
    }

    fun removeLastViewedList() {
        launch {
            userPreferencesRepository.removeLastViewedList()
        }
    }

    fun restoreLastViewedList() {
        launch {
            getLastViewedListInteractor()?.let {
                lastViewedListFlow.emit(it)
            }
        }
    }

    private fun saveDeepLinkList(link: Uri) {
        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.IO) {
            if (link.queryParameterNames.contains(Deeplink.firebaseIdParameter)) {
                doAuthRequiredWorkInteractor { saveDeepLinkOnlineList(link) }
            } else {
                saveDeepLinkOfflineList(link)
            }
        }
    }

    private suspend fun saveDeepLinkOnlineList(link: Uri) {
        val firebaseListId = link.getQueryParameter(Deeplink.firebaseIdParameter) ?: return

        if (whatToBuyListsDao.hasFirebaseList(firebaseListId)) {
            listAlreadySharedEventFlow.emit(Unit)
            return
        }

        val data = firebaseDb
            .getShareListReference(firebaseListId)
            .pullSharedList(firebaseListId)
            ?: run {
                badListEventFlow.emit(Unit)
                return
            }

        saveNewListInteractor.invoke(data.list, data.items)
    }

    private suspend fun saveDeepLinkOfflineList(link: Uri) {
        val encryptedData = link.getQueryParameter(Deeplink.dataParameter) ?: return
        val json = encryptDecryptXOR(encryptedData)

        val data = Gson().fromJson(json, WhatToBuyShareOffline::class.java)

        if (whatToBuyListsDao.hasSharedList(data.uuid)) {
            listAlreadySharedEventFlow.emit(Unit)
            return
        }

        val metadata = WhatToBuyList(
            isShared = true,
            title = data.listTitle,
            shareUuid = data.uuid,
        )

        val items = data.items.map { WhatToBuy(title = it.title, orderInShop = it.positionInShop) }
        saveNewListInteractor(metadata, items)
    }
}