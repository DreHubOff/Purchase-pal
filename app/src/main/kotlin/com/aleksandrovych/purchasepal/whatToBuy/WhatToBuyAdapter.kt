package com.aleksandrovych.purchasepal.whatToBuy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.aleksandrovych.purchasepal.databinding.ItemWhatToByBinding

class WhatToBuyAdapter(
    private val onItemChecked: (Boolean, WhatToBuy) -> Unit,
    private val onRemoveItem: (WhatToBuy) -> Unit,
) : ListAdapter<WhatToBuy, WhatToBuyAdapter.WhatToBuyViewHolder>(
    object : DiffUtil.ItemCallback<WhatToBuy>() {
        override fun areItemsTheSame(oldItem: WhatToBuy, newItem: WhatToBuy) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WhatToBuy, newItem: WhatToBuy) = oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhatToBuyViewHolder {
        return WhatToBuyViewHolder(
            ItemWhatToByBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(holder: WhatToBuyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WhatToBuyViewHolder(
        private val binding: ItemWhatToByBinding
    ) : ViewHolder(binding.root) {

        private var item: WhatToBuy? = null

        init {
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                item?.let { onItemChecked(isChecked, it) }
            }
            binding.deleteButton.setOnClickListener { item?.let { onRemoveItem(item!!) } }
        }

        fun bind(item: WhatToBuy) {
            this.item = item
            binding.checkBox.alpha = if (item.done) 0.4f else 1f
            binding.checkBox.isChecked = item.done
            binding.checkBox.text = item.title
            binding.positionInShopTextView.text = item.orderInShop.toString()
        }
    }
}