package com.aleksandrovych.purchasepal.domain

import com.aleksandrovych.purchasepal.FirebaseDatabase.getShareListReference
import com.aleksandrovych.purchasepal.FirebaseDatabase.pushSharedItems
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuyDao
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestion
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestionDao
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID
import javax.inject.Inject

class SaveWhatToBuyInteractor @Inject constructor(
    private val whatToBuyDao: WhatToBuyDao,
    private val whatToBuyListsDao: WhatToBuyListsDao,
    private val whatToBuySuggestionDao: WhatToBuySuggestionDao,
    private val firebaseDb: FirebaseDatabase,
    private val doAuthRequiredWorkInteractor: DoAuthRequiredWorkInteractor,
) {

    suspend operator fun invoke(vararg items: WhatToBuy) {
        items.ifEmpty { return }
        items
            .toList()
            .map { it.copy(title = WhatToBuySuggestion.processInput(it.title)) }
            .also { whatToBuyDao.insert(it) }
            .map { WhatToBuySuggestion.fromInput(it.title) }
            .also { whatToBuySuggestionDao.insert(it) }

        val metadata = whatToBuyListsDao.getByIdWithMetadata(items.first().listId)
        val listPublicId = metadata.list.firebaseId
        if (listPublicId.isNullOrEmpty()) return

        val publicItems = metadata
            .items
            .filter { it.uniquePublicId.isNullOrEmpty() }
            .map { it.copy(uniquePublicId = UUID.randomUUID().toString()) }
            .toTypedArray()
        publicItems.forEach { whatToBuyDao.updateUniquePublicIdById(it.id, it.uniquePublicId) }

        doAuthRequiredWorkInteractor {
            firebaseDb.getShareListReference(listPublicId).pushSharedItems(*publicItems)
        }
    }
}