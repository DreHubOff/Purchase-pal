package com.aleksandrovych.purchasepal.whatToBuy.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksandrovych.purchasepal.domain.SaveWhatToBuyInteractor
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestion
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddWhatToBuyViewModel @Inject constructor(
    private val whatToBuySuggestionDao: WhatToBuySuggestionDao,
    private val saveWhatToBuyInteractor: SaveWhatToBuyInteractor,
) : ViewModel() {

    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }

    val suggestionsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    fun requestSuggestions(input: String?) {
        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {
            suggestionsFlow.emit(
                if (input.isNullOrEmpty()) {
                    emptyList()
                } else {
                    whatToBuySuggestionDao
                        .get(WhatToBuySuggestion.processInput(input))
                        .map(WhatToBuySuggestion::title)
                }
            )
        }
    }

    fun saveNewItem(item: WhatToBuy, dismissCallback: () -> Unit) {
        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Default) {
            saveWhatToBuyInteractor(item)
            withContext(Dispatchers.Main) { dismissCallback() }
        }
    }
}