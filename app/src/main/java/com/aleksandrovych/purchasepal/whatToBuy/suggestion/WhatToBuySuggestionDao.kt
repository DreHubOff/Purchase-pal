package com.aleksandrovych.purchasepal.whatToBuy.suggestion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WhatToBuySuggestionDao {

    @Query(
        "SELECT * FROM what_to_buy_suggestion " +
                "WHERE first_symbol_code = :firstSymbolCode " +
                "AND title LIKE :query || '%' " +
                "ORDER BY title ASC " +
                "LIMIT :limit"
    )
    suspend fun get(
        query: String,
        firstSymbolCode: Int = query.firstOrNull()?.code ?: WhatToBuySuggestion.INVALID_CODE,
        limit: Int = 5,
    ): List<WhatToBuySuggestion>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: WhatToBuySuggestion)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(items: List<WhatToBuySuggestion>)
}