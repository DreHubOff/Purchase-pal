package com.aleksandrovych.purchasepal.lists.add

import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.ui.base.BaseViewModel
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@HiltViewModel
class AddWhatToBuyListViewModel @Inject constructor(
    private val whatToBuyListsDao: WhatToBuyListsDao,
) : BaseViewModel() {

    val dismissFlow = MutableSharedFlow<Unit>()

    fun saveList(title: String) {
        launch {
            whatToBuyListsDao.insert(WhatToBuyList(title = WhatToBuySuggestion.processInput(title)))
            dismissFlow.emit(Unit)
        }
    }
}