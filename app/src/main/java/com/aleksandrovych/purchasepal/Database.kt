package com.aleksandrovych.purchasepal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuyDao
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestion
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [
        WhatToBuy::class,
        WhatToBuySuggestion::class,
        WhatToBuyList::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class Database : RoomDatabase() {

    abstract fun whatToBuyDao(): WhatToBuyDao
    abstract fun whatToBuySuggestionDao(): WhatToBuySuggestionDao
    abstract fun whatToBuyListsDao(): WhatToBuyListsDao

    @Module
    @InstallIn(SingletonComponent::class)
    object DBModule {

        @Provides
        @Singleton
        fun getDatabase(@ApplicationContext context: Context): com.aleksandrovych.purchasepal.Database =
            Room.databaseBuilder(
                context.applicationContext,
                com.aleksandrovych.purchasepal.Database::class.java,
                "purchase_pal_database"
            ).build()

        @Provides
        fun provideWhatToBuyDao(db: com.aleksandrovych.purchasepal.Database) = db.whatToBuyDao()

        @Provides
        fun provideWhatToBuySuggestionDao(db: com.aleksandrovych.purchasepal.Database) =
            db.whatToBuySuggestionDao()

        @Provides
        fun provideWhatToBuyListsDao(db: com.aleksandrovych.purchasepal.Database) =
            db.whatToBuyListsDao()
    }
}