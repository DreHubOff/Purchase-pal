package com.aleksandrovych.purchasepal.data.resources.remote

import com.aleksandrovych.purchasepal.lists.WhatToBuyList
import com.aleksandrovych.purchasepal.lists.WhatToBuyListWithItems
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

object FirebaseDatabase {

    private const val SHARE_PATH_REFERENCE = "share"

    private const val LIST_TITLE_KEY = "title"
    private const val LIST_DATA_KEY = "list"
    private const val LIST_CREATION_DATE = "creation_date"

    fun FirebaseDatabase.getShareListReference(recordId: String): DatabaseReference =
        getReference(SHARE_PATH_REFERENCE).child(recordId)

    suspend fun DatabaseReference.pushSharedList(data: WhatToBuyListWithItems) {
        setValue(
            mapOf(
                LIST_TITLE_KEY to data.list.title,
                LIST_CREATION_DATE to data.list.creationDate,
                LIST_DATA_KEY to mapToFirebaseDbRecord(data.items)
            )
        ).await()
    }

    suspend fun DatabaseReference.deleteSharedList(): Void = removeValue().await()

    suspend fun DatabaseReference.pushSharedItems(vararg items: WhatToBuy) {
        items.forEach {
            child(LIST_DATA_KEY)
                .child(it.uniquePublicId.orEmpty())
                .setValue(WhatToByDbRecord.toMap(it))
                .await()
        }
    }

    suspend fun DatabaseReference.deleteSharedItem(item: WhatToBuy) {
        child(LIST_DATA_KEY).child(item.uniquePublicId.orEmpty()).removeValue().await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun DatabaseReference.pullSharedList(recordId: String): WhatToBuyListWithItems? {
        val snapSnapshot: DataSnapshot = get().await()
        val data = snapSnapshot.value as? Map<Any, Any> ?: return null
        val listTitle = data[LIST_TITLE_KEY] as? String ?: return null
        val listCreationDate = data[LIST_CREATION_DATE] as? Long ?: return null

        val listMetadata = WhatToBuyList(
            title = listTitle,
            isShared = true,
            firebaseId = recordId,
            creationDate = listCreationDate
        )
        return WhatToBuyListWithItems(listMetadata, dataSnapshotToItems(snapSnapshot))
    }

    @Suppress("UNCHECKED_CAST")
    fun dataSnapshotToItems(snapSnapshot: DataSnapshot): List<WhatToBuy> {
        val data = snapSnapshot.value as? Map<Any, Any> ?: return emptyList()
        val listData = data[LIST_DATA_KEY] as? Map<String, Map<String, Any>> ?: return emptyList()

        return listData
            .toList()
            .map { (publicId, dbRecord) -> WhatToByDbRecord.fromMap(publicId, dbRecord) }
    }

    private fun mapToFirebaseDbRecord(list: List<WhatToBuy>) =
        list.associate { requireNotNull(it.uniquePublicId) to WhatToByDbRecord.toMap(it) }

    private object WhatToByDbRecord {

        private const val TITLE_KEY = "title"
        private const val ORDER_IN_SHOP_KEY = "order_in_shop"
        private const val COMMENTS_KEY = "comments"
        private const val DONE_KEY = "done"

        fun toMap(item: WhatToBuy) = mapOf(
            TITLE_KEY to item.title,
            ORDER_IN_SHOP_KEY to item.orderInShop,
            COMMENTS_KEY to item.comments,
            DONE_KEY to item.done
        )

        fun fromMap(id: String, data: Map<String, Any>) = WhatToBuy(
            title = data[TITLE_KEY] as String,
            orderInShop = (data[ORDER_IN_SHOP_KEY] as Number).toInt(),
            listId = -1,
            comments = data[COMMENTS_KEY] as? String,
            done = data[DONE_KEY] as Boolean,
            uniquePublicId = id,
        )
    }
}