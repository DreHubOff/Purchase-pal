package com.aleksandrovych.purchasepal.lists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.databinding.ItemWhatToByListBinding

class WhatToBuyListsAdapter(
    private val onItemClicked: (WhatToBuyList) -> Unit,
    private val onDeleteClicked: (WhatToBuyList) -> Unit,
) : ListAdapter<WhatToBuyListWithItems, WhatToBuyListsAdapter.WhatToBuyListViewHolder>(
    object : DiffUtil.ItemCallback<WhatToBuyListWithItems>() {
        override fun areItemsTheSame(
            oldItem: WhatToBuyListWithItems,
            newItem: WhatToBuyListWithItems,
        ) = oldItem.list.id == newItem.list.id

        override fun areContentsTheSame(
            oldItem: WhatToBuyListWithItems,
            newItem: WhatToBuyListWithItems,
        ) = oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhatToBuyListViewHolder {
        return WhatToBuyListViewHolder(
            ItemWhatToByListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(holder: WhatToBuyListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WhatToBuyListViewHolder(
        private val binding: ItemWhatToByListBinding,
    ) : ViewHolder(binding.root) {

        private var item: WhatToBuyListWithItems? = null

        init {
            binding.deleteButton.setOnClickListener { item?.let { onDeleteClicked(item!!.list) } }
            binding.container.setOnClickListener { item?.let { onItemClicked(item!!.list) } }
        }

        fun bind(item: WhatToBuyListWithItems) {
            this.item = item
            binding.titleTextView.text = item.list.title
            binding.offlineButton.isGone =
                !item.list.isShared || !item.list.firebaseId.isNullOrEmpty()
            binding.onlineButton.isGone = item.list.firebaseId.isNullOrEmpty()
            binding.progressTextView.text = itemView
                .resources
                .getString(R.string.pattern_list_progress, item.doneItemsCount, item.totalCount)
        }
    }
}