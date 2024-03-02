package com.aleksandrovych.purchasepal.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.aleksandrovych.purchasepal.databinding.FragmentWhatToByListsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
                    .setMessage("Вы уверены, что хотите удалить \"${list.title}\"?")
                    .setPositiveButton("Да") { _, _ -> viewModel.delete(list) }
                    .setNegativeButton("Нет") { _, _ -> }
                    .show()
            }
        )
        binding.recyclerView.adapter = adapter

        binding.addButton.setOnClickListener {
            val action = WhatToBuyListsFragmentDirections
                .actionWhatToBuyListsFragmentToAddWhatToBuyListDialog(WhatToBuyList())
            findNavController().navigate(action)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.observeLists().collect { list -> adapter?.submitList(list) }
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        adapter = null
        super.onDestroyView()
    }
}