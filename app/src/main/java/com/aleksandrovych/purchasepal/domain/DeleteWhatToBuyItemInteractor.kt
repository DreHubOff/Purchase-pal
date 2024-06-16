package com.aleksandrovych.purchasepal.domain

import com.aleksandrovych.purchasepal.data.resources.remote.FirebaseDatabase.deleteSharedItem
import com.aleksandrovych.purchasepal.data.resources.remote.FirebaseDatabase.getShareListReference
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuyDao
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class DeleteWhatToBuyItemInteractor @Inject constructor(
    private val whatToBuyDao: WhatToBuyDao,
    private val whatToBuyListsDao: WhatToBuyListsDao,
    private val firebaseDb: FirebaseDatabase,
    private val doAuthRequiredWorkInteractor: DoAuthRequiredWorkInteractor,
) {

    suspend operator fun invoke(item: WhatToBuy) {
        whatToBuyDao.delete(item)
        val publicListId = whatToBuyListsDao.getById(item.listId).firebaseId
        if (publicListId.isNullOrEmpty() || item.uniquePublicId.isNullOrEmpty()) return
        doAuthRequiredWorkInteractor {
            firebaseDb.getShareListReference(publicListId).deleteSharedItem(item)
        }
    }
}