package com.aleksandrovych.purchasepal

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListsDao
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuyDao
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestion
import com.aleksandrovych.purchasepal.whatToBuy.suggestion.WhatToBuySuggestionDao

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
}