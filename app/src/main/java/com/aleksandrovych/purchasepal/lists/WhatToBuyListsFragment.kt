package com.aleksandrovych.purchasepal.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.databinding.FragmentWhatToByListsBinding
import com.aleksandrovych.purchasepal.extensions.launchWhenResumed
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WhatToBuyListsFragment : Fragment() {

    private val viewModel: WhatToBuyListsViewModel by viewModels()
    private var binding: FragmentWhatToByListsBinding? = null
    private var adapter: WhatToBuyListsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWhatToByListsBinding.inflate(
            inflater,
            container,
            false
        ).apply(::onViewCreated)
        return binding!!.root
    }

    private fun onViewCreated(binding: FragmentWhatToByListsBinding) {
        adapter = WhatToBuyListsAdapter(
            onItemClicked = { list ->
                val action = WhatToBuyListsFragmentDirections
                    .actionWhatToBuyListsFragmentToWhatToBuyFragment(list)
                findNavController().navigate(action)
            },
            onDeleteClicked = { list ->
                MaterialAlertDialogBuilder(binding.root.context)
                    .setMessage(getString(R.string.message_confirm_item_deletion, list.title))
                    .setPositiveButton(R.string.yes) { _, _ -> viewModel.delete(list) }
                    .setNegativeButton(R.string.no) { _, _ -> }
                    .show()
            }
        )
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

    override fun onDestroyView() {
        binding = null
        adapter = null
        super.onDestroyView()
    }
}