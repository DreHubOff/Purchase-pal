package com.aleksandrovych.purchasepal.whatToBuy

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Suppress("ProtectedInFinal")
@Parcelize
@Entity(tableName = "what_to_buy")
data class WhatToBuy(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    protected val _id: Int? = null,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "order_in_shop")
    val orderInShop: Int,

    @ColumnInfo(name = LIST_ID_COLUMN_NAME)
    val listId: Int = -1,

    @ColumnInfo(name = "comments")
    val comments: String? = null,

    @ColumnInfo(name = "done")
    val done: Boolean = false,

    @ColumnInfo(name = "unique_public_id")
    val uniquePublicId: String? = null,
) : Parcelable {

    @IgnoredOnParcel
    @get:Ignore
    val id: Int get() = checkNotNull(_id)

    companion object {

        const val LIST_ID_COLUMN_NAME = "list_id"
    }
}