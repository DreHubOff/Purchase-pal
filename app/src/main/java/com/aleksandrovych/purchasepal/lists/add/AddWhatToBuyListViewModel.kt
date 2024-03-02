package com.aleksandrovych.purchasepal.lists.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWhatToBuyListViewModel @Inject constructor(
    private val whatToBuyListsDao: WhatToBuyListsDao,
) : ViewModel() {

    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }

    val dismissFlow = MutableSharedFlow<Unit>()

    fun saveList(title: String) {
        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {
            whatToBuyListsDao.insert(WhatToBuyList(title = WhatToBuySuggestion.processInput(title)))
            dismissFlow.emit(Unit)
        }
    }
}