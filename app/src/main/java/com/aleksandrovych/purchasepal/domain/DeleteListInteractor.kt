package com.aleksandrovych.purchasepal.domain

import com.aleksandrovych.purchasepal.FirebaseDatabase.deleteSharedList
import com.aleksandrovych.purchasepal.FirebaseDatabase.getShareListReference
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuyDao
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class DeleteListInteractor @Inject constructor(
    private val whatToBuyDao: WhatToBuyDao,
    private val whatToBuyListsDao: WhatToBuyListsDao,
    private val firebaseDb: FirebaseDatabase,
    private val doAuthRequiredWorkInteractor: DoAuthRequiredWorkInteractor,
) {

    suspend operator fun invoke(list: WhatToBuyList) {
        whatToBuyDao.delete(listId = list.id)
        whatToBuyListsDao.delete(list)
        val publicListId = list.firebaseId
        if (publicListId.isNullOrEmpty()) return
        doAuthRequiredWorkInteractor {
            firebaseDb.getShareListReference(publicListId).deleteSharedList()
        }
    }
}