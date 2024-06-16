package com.aleksandrovych.purchasepal.data.resources.local

import com.aleksandrovych.purchasepal.data.resources.PrimitiveStorage

private const val LAST_VIEWED_LIST_ID_KEY = "last_viewed_list_id"

class UserPreferencesStore(
    private val primitiveStorage: PrimitiveStorage,
) {

    suspend fun getLastViewedListId(): Int? = primitiveStorage.getValue(LAST_VIEWED_LIST_ID_KEY, Int::class)

    suspend fun saveLastViewedListId(listId: Int) {
        primitiveStorage.transaction { putValue(LAST_VIEWED_LIST_ID_KEY, listId) }
    }

    suspend fun removeLastViewedListId() {
        primitiveStorage.transaction { remove(LAST_VIEWED_LIST_ID_KEY) }
    }

    companion object {
        const val STORE_FILE_NAME = "user_preference_store"
    }
}