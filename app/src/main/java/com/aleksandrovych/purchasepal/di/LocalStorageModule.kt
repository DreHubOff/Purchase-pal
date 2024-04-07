package com.aleksandrovych.purchasepal.di

import android.content.Context
import androidx.room.Room
import com.aleksandrovych.purchasepal.Database
import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.ResourceProvider
import com.aleksandrovych.purchasepal.ResourceProvider.Companion.toStringPointer
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuyDao
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalStorageModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        resourceProvider: ResourceProvider,
    ): Database =
        Room.databaseBuilder(
            context.applicationContext,
            Database::class.java,
            resourceProvider[R.string.purchase_pal_database_name.toStringPointer()]
        ).build()

    @Provides
    fun provideWhatToBuyDao(db: Database): WhatToBuyDao = db.whatToBuyDao()

    @Provides
    fun provideWhatToBuySuggestionDao(db: Database): WhatToBuySuggestionDao = db.whatToBuySuggestionDao()

    @Provides
    fun provideWhatToBuyListsDao(db: Database): WhatToBuyListsDao = db.whatToBuyListsDao()
}