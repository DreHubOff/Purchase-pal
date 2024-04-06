package com.aleksandrovych.purchasepal.whatToBuy

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.databinding.FragmentWhatToByBinding
import com.aleksandrovych.purchasepal.extensions.launchWhenResumed
import com.aleksandrovych.purchasepal.extensions.lifecycle
import com.aleksandrovych.purchasepal.ui.base.BaseFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest

class WhatToBuyFragment : BaseFragment<FragmentWhatToByBinding>() {

    private val viewModel: WhatToBuyViewModel by viewModels()
    private val adapter: WhatToBuyAdapter? by lifecycle(
        releaseAction = { binding?.recyclerView?.adapter = null },
    ) {
        WhatToBuyAdapter(
            onItemChecked = viewModel::updateCheckedItem,
            onRemoveItem = { item ->
                context
                    ?.let(::MaterialAlertDialogBuilder)
                    ?.setMessage(getString(R.string.message_confirm_item_deletion, item.title))
                    ?.setPositiveButton(R.string.yes) { _, _ -> viewModel.delete(item) }
                    ?.setNegativeButton(R.string.no) { _, _ -> }
                    ?.show()
            }
        )
    }

    private val args: WhatToBuyFragmentArgs by navArgs()

    override fun onBindingCreated(binding: FragmentWhatToByBinding) {
        binding.toolbar.title = args.whatToBuyListArg.title

        binding.recyclerView.adapter = adapter
        binding.addButton.setOnClickListener {
            val newItemPosition = (adapter?.currentList?.lastOrNull()?.orderInShop ?: -1) + 1
            val action = WhatToBuyFragmentDirections.actionWhatToBuyFragmentToAddWhatToBuyDialog(
                positionPrefillArg = newItemPosition,
                listIdArg = args.whatToBuyListArg.id,
            )
            findNavController().navigate(action)
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.share_item_id -> {
                    MaterialAlertDialogBuilder(binding.root.context)
                        .setMessage(R.string.message_confirm_joint_list_editing)
                        .setPositiveButton(R.string.yes) { _, _ -> share(offline = false) }
                        .setNegativeButton(R.string.no) { _, _ -> share(offline = true) }
                        .show()
                    true
                }

                else -> false
            }
        }

        launchWhenResumed {
            viewModel.observeItems(args.whatToBuyListArg).collectLatest { list ->
                adapter?.submitList(list)
            }
        }

        launchWhenResumed {
            viewModel.badListEventFlow.collect {
                MaterialAlertDialogBuilder(binding.root.context)
                    .setMessage(R.string.message_list_damaged_or_removed_would_you_like_to_save_it)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        viewModel.mapListToLocal(args.whatToBuyListArg)
                    }
                    .setNegativeButton(R.string.no) { _, _ ->
                        viewModel.deleteCurrentList(args.whatToBuyListArg)
                    }
                    .show()
            }
        }

        launchWhenResumed {
            viewModel.onListRemovedEventFlow.collect { findNavController().popBackStack() }
        }
    }

    override fun onPause() {
        viewModel.releaseObservers()
        super.onPause()
    }

    private fun share(offline: Boolean) {
        viewModel.shareList(adapter?.currentList.orEmpty(), requireActivity(), offline)
    }
}