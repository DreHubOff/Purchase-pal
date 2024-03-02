package com.aleksandrovych.purchasepal.whatToBuy

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatToBuyDao {

    @Query("SELECT * FROM what_to_buy WHERE list_id = :listId ORDER BY done ASC, order_in_shop ASC")
    fun observe(listId: Int = 0): Flow<List<WhatToBuy>>

    @Query("SELECT * FROM what_to_buy WHERE list_id = :listId ORDER BY done ASC, order_in_shop ASC")
    suspend fun getByListId(listId: Int = 0): List<WhatToBuy>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WhatToBuy)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<WhatToBuy>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: WhatToBuy)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(items: List<WhatToBuy>)

    @Query("UPDATE what_to_buy SET unique_public_id = :newUniquePublicId WHERE id = :id")
    suspend fun updateUniquePublicIdById(id: Int, newUniquePublicId: String?)

    @Query("DELETE FROM what_to_buy WHERE list_id = :listId")
    suspend fun delete(listId: Int)

    @Delete
    suspend fun delete(item: WhatToBuy)

    @Delete
    suspend fun delete(items: List<WhatToBuy>)

    @Transaction
    suspend fun performIUDOperation(
        itemsToSave: List<WhatToBuy>,
        itemsToUpdate: List<WhatToBuy>,
        itemsToDelete: List<WhatToBuy>,
    ) {
        insert(itemsToSave)
        update(itemsToUpdate)
        delete(itemsToDelete)
    }
}