package com.aleksandrovych.purchasepal.domain

import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import javax.inject.Inject

class SaveNewListInteractor @Inject constructor(
    private val whatToByListDao: WhatToBuyListsDao,
    private val saveWhatToBuyInteractor: SaveWhatToBuyInteractor,
) {

    suspend operator fun invoke(metadata: WhatToBuyList, list: List<WhatToBuy>) {
        val listId = whatToByListDao.insert(metadata)
        saveWhatToBuyInteractor(*list.map { it.copy(listId = listId.toInt()) }.toTypedArray())
    }
}