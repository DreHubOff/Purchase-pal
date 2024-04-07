package com.aleksandrovych.purchasepal.whatToBuy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import com.aleksandrovych.purchasepal.BuildConfig
import com.aleksandrovych.purchasepal.Cipher.encryptDecryptXOR
import com.aleksandrovych.purchasepal.Deeplink
import com.aleksandrovych.purchasepal.FirebaseDatabase.getShareListReference
import com.aleksandrovych.purchasepal.FirebaseDatabase.pushSharedList
import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.ResourceProvider
import com.aleksandrovych.purchasepal.ResourceProvider.Companion.toStringPointer
import com.aleksandrovych.purchasepal.VibratorManager
import com.aleksandrovych.purchasepal.domain.DeleteListInteractor
import com.aleksandrovych.purchasepal.domain.DeleteWhatToBuyItemInteractor
import com.aleksandrovych.purchasepal.domain.DoAuthRequiredWorkInteractor
import com.aleksandrovych.purchasepal.domain.UpdateWhatToBuyItemInteractor
import com.aleksandrovych.purchasepal.extensions.SingleTypeViewModelFactory
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListWithItems
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.ui.base.BaseViewModel
import com.aleksandrovych.purchasepal.whatToBuy.share.WhatToBuyShareOffline
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class WhatToBuyViewModel @AssistedInject constructor(
    @Assisted private val whatToBuyList: WhatToBuyList,
    private val whatToBuyDao: WhatToBuyDao,
    private val whatToBuyListDao: WhatToBuyListsDao,
    private val firebaseDb: FirebaseDatabase,
    private val doAuthRequiredWorkInteractor: DoAuthRequiredWorkInteractor,
    private val updateWhatToBuyItemInteractor: UpdateWhatToBuyItemInteractor,
    private val deleteWhatToBuyItemInteractor: DeleteWhatToBuyItemInteractor,
    private val deleteListInteractor: DeleteListInteractor,
    private val vibratorManager: VibratorManager,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel() {

    private var observeRemoteItemsJob: Job? = null
    val badListEventFlow = MutableSharedFlow<Unit>()
    val onListRemovedEventFlow = MutableSharedFlow<Unit>()

    fun observeItems() = whatToBuyDao.observe(whatToBuyList.id).onEach { _ ->
        if (observeRemoteItemsJob?.isActive == true) return@onEach
        val actualList = whatToBuyListDao.getById(whatToBuyList.id)
        val firebaseId = actualList.firebaseId
        if (!firebaseId.isNullOrEmpty()) {
            doAuthRequiredWorkInteractor { observeRemoteItems(firebaseId, whatToBuyList.id) }
        }
    }

    private fun observeRemoteItems(firebaseListId: String, localListId: Int) {
        observeRemoteItemsJob = launch {

            callbackFlow {
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            notifyListRemoved()
                            return
                        }
                        trySendBlocking(snapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        error.toException().printStackTrace()
                    }
                }.also(firebaseDb.getShareListReference(firebaseListId)::addValueEventListener)

                awaitClose {
                    firebaseDb.getShareListReference(firebaseListId)
                        .removeEventListener(listener)
                }
            }
                .map(com.aleksandrovych.purchasepal.FirebaseDatabase::dataSnapshotToItems)
                .collectLatest { saveRemoteItems(localListId, it) }
        }
    }

    private fun notifyListRemoved() {
        launch(coroutineContext + Dispatchers.Default) { badListEventFlow.emit(Unit) }
    }

    private suspend fun saveRemoteItems(localListId: Int, remoteList: List<WhatToBuy>) {
        val localList = whatToBuyDao.getByListId(localListId)
        val itemsToSave = mutableListOf<WhatToBuy>()
        val itemsToUpdate = mutableListOf<WhatToBuy>()
        val itemsToDelete = localList.filter {
            remoteList.none { remoteItem -> remoteItem.uniquePublicId == it.uniquePublicId }
        }
        remoteList.map { it.copy(listId = localListId) }.forEach { remoteItem ->
            val localItem = localList.firstOrNull { it.uniquePublicId == remoteItem.uniquePublicId }
            if (localItem == null) {
                itemsToSave += remoteItem
                return@forEach
            }
            val remoteCopy = remoteItem.copy(_id = localItem.id)
            if (remoteCopy != localItem) {
                itemsToUpdate += remoteCopy
            }
        }
        whatToBuyDao.performIUDOperation(itemsToSave, itemsToUpdate, itemsToDelete)
        if (itemsToSave.isNotEmpty() || itemsToUpdate.isNotEmpty() || itemsToDelete.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                vibratorManager.vibrateDevice()
            }
        }
    }

    fun updateCheckedItem(checked: Boolean, item: WhatToBuy) {
        launch {
            updateWhatToBuyItemInteractor(item.copy(done = checked))
        }
    }

    fun delete(item: WhatToBuy) {
        launch {
            deleteWhatToBuyItemInteractor(item)
        }
    }

    fun shareList(list: List<WhatToBuy>, activity: Activity, offline: Boolean) {
        list.ifEmpty { return }
        launch {
            val deepLink = if (offline) {
                getOfflineDeeplinkUrl(list)
            } else {
                getOnlineDeeplinkUrl(list)
            }


            val shortLink = Firebase.dynamicLinks.shortLinkAsync {
                link = deepLink
                domainUriPrefix = BuildConfig.FIREBASE_DEEPLINK_DOMAIN

                androidParameters {
                    minimumVersion = 1
                }
            }.await().shortLink

            val message = resourceProvider[R.string.share_list_title_hint.toStringPointer(shortLink)]
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = Deeplink.mimeType
            intent.putExtra(Intent.EXTRA_TEXT, message)
            val chooserTitle = resourceProvider[R.string.share_using_some_app_title_hint.toStringPointer()]
            val chooser = Intent.createChooser(intent, chooserTitle)
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(chooser)
            }
        }
    }

    private suspend fun getOfflineDeeplinkUrl(list: List<WhatToBuy>): Uri {
        val json = WhatToBuyShareOffline(
            uuid = UUID.randomUUID().toString(),
            listTitle = whatToBuyListDao.getById(list.first().listId).title,
            items = list.map { WhatToBuyShareOffline.Item(it.title, it.orderInShop) },
        ).let(Gson()::toJson)

        return Uri.Builder()
            .scheme(Deeplink.scheme)
            .authority(Deeplink.host)
            .appendQueryParameter(Deeplink.dataParameter, encryptDecryptXOR(json))
            .build()
    }

    private suspend fun getOnlineDeeplinkUrl(list: List<WhatToBuy>): Uri {
        val listMetadata = whatToBuyListDao.getById(list.first().listId)
        val firebaseId = listMetadata.firebaseId.orEmpty().ifEmpty {
            val publicList = list.map { it.copy(uniquePublicId = UUID.randomUUID().toString()) }
            val id = saveListToFirebase(WhatToBuyListWithItems(listMetadata, publicList))
            val publicListMetadata = listMetadata.copy(firebaseId = id, isShared = true)
            saveOnlineList(publicListMetadata, publicList)
            return@ifEmpty id
        }

        return Uri.Builder()
            .scheme(Deeplink.scheme)
            .authority(Deeplink.host)
            .path(Deeplink.sharePath)
            .appendQueryParameter(Deeplink.firebaseIdParameter, firebaseId)
            .build()
    }

    private suspend fun saveOnlineList(listMetadata: WhatToBuyList, list: List<WhatToBuy>) {
        whatToBuyDao.update(list)
        whatToBuyListDao.update(listMetadata)
    }

    private suspend fun saveListToFirebase(list: WhatToBuyListWithItems): String {
        return doAuthRequiredWorkInteractor {
            val shareRecordId = UUID.randomUUID().toString()
            firebaseDb.getShareListReference(shareRecordId).pushSharedList(list)
            return@doAuthRequiredWorkInteractor shareRecordId
        }
    }

    fun releaseObservers() {
        observeRemoteItemsJob?.cancel()
        observeRemoteItemsJob = null
    }

    fun deleteCurrentList() {
        launch {
            deleteListInteractor(whatToBuyList)
            onListRemovedEventFlow.emit(Unit)
        }
    }

    fun mapListToLocal() {
        launch {
            releaseObservers()
            val localMetadata = whatToBuyList.copy(isShared = false, shareUuid = null, firebaseId = null)
            val localList = whatToBuyDao.getByListId(whatToBuyList.id).map { it.copy(uniquePublicId = null) }
            whatToBuyDao.update(localList)
            whatToBuyListDao.update(localMetadata)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(list: WhatToBuyList): WhatToBuyViewModel
    }

    companion object {
        fun Factory.asViewModelFactory(list: WhatToBuyList): ViewModelProvider.Factory =
            SingleTypeViewModelFactory { create(list) }
    }
}