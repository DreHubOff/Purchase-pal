package com.aleksandrovych.purchasepal.lists

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatToBuyListsDao {

    @Transaction
    @Query("SELECT * FROM what_to_buy_list ORDER BY creation_date DESC")
    fun observe(): Flow<List<WhatToBuyListWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(whatToBuyList: WhatToBuyList): Long

    @Delete
    suspend fun delete(list: WhatToBuyList)

    @Query("SELECT * FROM what_to_buy_list WHERE id = :id")
    suspend fun getById(id: Int): WhatToBuyList

    @Transaction
    @Query("SELECT * FROM what_to_buy_list WHERE id = :id")
    suspend fun getByIdWithMetadata(id: Int): WhatToBuyListWithItems

    @Query("SELECT COUNT(*) FROM what_to_buy_list WHERE share_uuid = :listUuid")
    suspend fun hasSharedList(listUuid: String): Boolean

    @Query("SELECT COUNT(*) FROM what_to_buy_list WHERE firebase_id = :firebaseId")
    suspend fun hasFirebaseList(firebaseId: String): Boolean

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(listMetadata: WhatToBuyList)
}