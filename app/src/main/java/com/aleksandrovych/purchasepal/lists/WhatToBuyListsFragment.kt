package com.aleksandrovych.purchasepal.lists

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.databinding.FragmentWhatToByListsBinding
import com.aleksandrovych.purchasepal.extensions.launchWhenResumed
import com.aleksandrovych.purchasepal.extensions.lifecycle
import com.aleksandrovych.purchasepal.ui.base.BaseFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WhatToBuyListsFragment : BaseFragment<FragmentWhatToByListsBinding>() {

    private val viewModel: WhatToBuyListsViewModel by viewModels()
    private val adapter: WhatToBuyListsAdapter? by lifecycle(
        releaseAction = { binding?.recyclerView?.adapter = null },
        initializer = ::createListAdapter,
    )

    override fun onBindingCreated(binding: FragmentWhatToByListsBinding) {
        binding.recyclerView.adapter = adapter

        binding.addButton.setOnClickListener {
            viewModel.prepareEmptyList()
        }

        launchWhenResumed {
            viewModel.observeLists().collect { list -> adapter?.submitList(list) }
        }

        launchWhenResumed {
            viewModel.emptyListFlow.collect { list ->
                val action = WhatToBuyListsFragmentDirections
                    .actionWhatToBuyListsFragmentToAddWhatToBuyListDialog(list)
                findNavController().navigate(action)
            }
        }
    }

    private fun createListAdapter(): WhatToBuyListsAdapter {
        return WhatToBuyListsAdapter(
            onItemClicked = { list ->
                val action = WhatToBuyListsFragmentDirections
                    .actionWhatToBuyListsFragmentToWhatToBuyFragment(list)
                findNavController().navigate(action)
            },
            onDeleteClicked = { list ->
                context
                    ?.let(::MaterialAlertDialogBuilder)
                    ?.setMessage(getString(R.string.message_confirm_item_deletion, list.title))
                    ?.setPositiveButton(R.string.yes) { _, _ -> viewModel.delete(list) }
                    ?.setNegativeButton(R.string.no) { _, _ -> }
                    ?.show()
            }
        )
    }
}