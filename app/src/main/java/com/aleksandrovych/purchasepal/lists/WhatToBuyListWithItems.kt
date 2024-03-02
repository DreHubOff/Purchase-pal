package com.aleksandrovych.purchasepal.lists

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import kotlinx.parcelize.IgnoredOnParcel

data class WhatToBuyListWithItems(
    @Embedded val list: WhatToBuyList,
    @Relation(
        parentColumn = WhatToBuyList.ID_COLUMN_NAME,
        entityColumn = WhatToBuy.LIST_ID_COLUMN_NAME,
    )
    val items: List<WhatToBuy>,
) {

    @get:Ignore
    @IgnoredOnParcel
    val doneItemsCount: Int get() = items.count(WhatToBuy::done)

    @get:Ignore
    @IgnoredOnParcel
    val totalCount: Int get() = items.count()
}