package com.aleksandrovych.purchasepal.domain

import com.aleksandrovych.purchasepal.data.resources.remote.FirebaseDatabase.getShareListReference
import com.aleksandrovych.purchasepal.data.resources.remote.FirebaseDatabase.pushSharedItems
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuyDao
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class UpdateWhatToBuyItemInteractor @Inject constructor(
    private val whatToBuyDao: WhatToBuyDao,
    private val whatToBuyListsDao: WhatToBuyListsDao,
    private val firebaseDb: FirebaseDatabase,
    private val doAuthRequiredWorkInteractor: DoAuthRequiredWorkInteractor,
) {

    suspend operator fun invoke(newItem: WhatToBuy) {
        whatToBuyDao.update(newItem)
        val listPublicId = whatToBuyListsDao.getById(newItem.listId).firebaseId
        if (listPublicId.isNullOrEmpty() || newItem.uniquePublicId.isNullOrEmpty()) return
        doAuthRequiredWorkInteractor {
            firebaseDb.getShareListReference(listPublicId).pushSharedItems(newItem)
        }
    }
}