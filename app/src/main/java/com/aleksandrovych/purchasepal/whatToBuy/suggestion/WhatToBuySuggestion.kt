package com.aleksandrovych.purchasepal.whatToBuy.suggestion

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Suppress("ProtectedInFinal")
@Parcelize
@Entity(tableName = "what_to_buy_suggestion")
data class WhatToBuySuggestion(

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "first_symbol_code", index = true)
    val firstSymbolCode: Int = title.firstOrNull()?.code ?: INVALID_CODE,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = title.hashCode(),
) : Parcelable {

    companion object {

        const val INVALID_CODE = Int.MIN_VALUE

        fun processInput(input: String) = input.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        fun fromInput(input: String) = WhatToBuySuggestion(processInput(input))
    }
}