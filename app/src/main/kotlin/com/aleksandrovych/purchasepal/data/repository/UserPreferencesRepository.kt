package com.aleksandrovych.purchasepal.data.repository

import com.aleksandrovych.purchasepal.data.resources.local.Database
import com.aleksandrovych.purchasepal.data.resources.local.UserPreferencesStore
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import dagger.Lazy
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val userPreferencesStore: UserPreferencesStore,
    private val database: Lazy<Database>,
) {

    suspend fun saveLastViewedList(list: WhatToBuyList) {
        userPreferencesStore.saveLastViewedListId(list.id)
    }

    suspend fun removeLastViewedList() {
        userPreferencesStore.removeLastViewedListId()
    }

    suspend fun getLastViewedList(): WhatToBuyList? {
        val id = userPreferencesStore.getLastViewedListId() ?: return null
        return database.get().whatToBuyListsDao().getById(id)
    }
}