package com.aleksandrovych.purchasepal.lists

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@Suppress("ProtectedInFinal")
@Entity(tableName = "what_to_buy_list")
data class WhatToBuyList(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN_NAME)
    protected val _id: Int? = null,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "is_shared")
    val isShared: Boolean = false,

    @ColumnInfo(name = "share_uuid")
    val shareUuid: String? = null,

    @ColumnInfo(name = "firebase_id")
    val firebaseId: String? = null,

    @ColumnInfo(name = "creation_date")
    val creationDate: Long = System.currentTimeMillis(),
) : Parcelable {

    @IgnoredOnParcel
    @get:Ignore
    val id: Int get() = checkNotNull(_id)

    companion object {

        const val ID_COLUMN_NAME = "id"
    }
}