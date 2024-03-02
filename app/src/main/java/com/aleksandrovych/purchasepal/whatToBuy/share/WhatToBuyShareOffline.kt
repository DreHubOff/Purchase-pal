package com.aleksandrovych.purchasepal.whatToBuy.share

import com.google.gson.annotations.SerializedName

class WhatToBuyShareOffline(

    @SerializedName("id")
    val uuid: String,

    @SerializedName("title")
    val listTitle: String,

    @SerializedName("list")
    val items: List<Item>,
) {

    class Item(
        @SerializedName("t")
        val title: String,
        @SerializedName("pis")
        val positionInShop: Int,
    )
}