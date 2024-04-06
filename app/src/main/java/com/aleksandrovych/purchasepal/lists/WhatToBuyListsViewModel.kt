package com.aleksandrovych.purchasepal.lists

import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.ResourceProvider
import com.aleksandrovych.purchasepal.ResourceProvider.Companion.toStringPointer
import com.aleksandrovych.purchasepal.domain.DeleteListInteractor
import com.aleksandrovych.purchasepal.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class WhatToBuyListsViewModel @Inject constructor(
    private val whatToBuyListsDao: WhatToBuyListsDao,
    private val deleteListInteractor: DeleteListInteractor,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel() {

    private val _emptyListFlow = MutableSharedFlow<WhatToBuyList>()
    val emptyListFlow: SharedFlow<WhatToBuyList> get() = _emptyListFlow

    fun observeLists() = whatToBuyListsDao.observe()

    fun delete(list: WhatToBuyList) {
        launch {
            deleteListInteractor(list)
        }
    }

    fun prepareEmptyList() {
        launch(coroutineContext + Dispatchers.Default) {
            val currentDate = formatDateToString(LocalDateTime.now())
            val defaultListName = resourceProvider[R.string.pattern_list_default_name.toStringPointer(currentDate)]
            _emptyListFlow.emit(WhatToBuyList(title = defaultListName))
        }
    }

    private fun formatDateToString(date: LocalDateTime): String {
        val format = resourceProvider[R.string.date_format_list_default_name.toStringPointer()]
        return date.format(DateTimeFormatter.ofPattern(format))
    }
}