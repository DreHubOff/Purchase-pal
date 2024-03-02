package com.aleksandrovych.purchasepal.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksandrovych.purchasepal.domain.DeleteListInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatToBuyListsViewModel @Inject constructor(
    private val whatToBuyListsDao: WhatToBuyListsDao,
    private val deleteListInteractor: DeleteListInteractor,
) : ViewModel() {

    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }

    fun observeLists() = whatToBuyListsDao.observe()

    fun delete(list: WhatToBuyList) {
        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.IO) {
            deleteListInteractor(list)
        }
    }
}