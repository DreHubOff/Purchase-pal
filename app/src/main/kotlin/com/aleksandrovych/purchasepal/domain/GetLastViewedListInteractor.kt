package com.aleksandrovych.purchasepal.domain

import com.aleksandrovych.purchasepal.data.repository.UserPreferencesRepository
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class GetLastViewedListInteractor @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    suspend operator fun invoke(): WhatToBuyList? = try {
        userPreferencesRepository.getLastViewedList()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        userPreferencesRepository.removeLastViewedList()
        null
    }
}