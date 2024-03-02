package com.aleksandrovych.purchasepal.whatToBuy

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
import androidx.navigation.fragment.navArgs
import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.databinding.FragmentWhatToByBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WhatToBuyFragment : Fragment() {

    private val viewModel: WhatToBuyViewModel by viewModels()
    private var binding: FragmentWhatToByBinding? = null
    private var adapter: WhatToBuyAdapter? = null

    private val args: WhatToBuyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWhatToByBinding.inflate(inflater, container, false)
            .apply(::onViewCreated)
        return binding!!.root
    }

    private fun onViewCreated(binding: FragmentWhatToByBinding) {
        binding.toolbar.title = args.whatToBuyListArg.title

        adapter = WhatToBuyAdapter(
            onItemChecked = viewModel::updateCheckedItem,
            onRemoveItem = { item ->
                MaterialAlertDialogBuilder(binding.root.context)
                    .setMessage("Вы уверены, что хотите удалить \"${item.title}\"?")
                    .setPositiveButton("Да") { _, _ -> viewModel.delete(item) }
                    .setNegativeButton("Нет") { _, _ -> }
                    .show()
            }
        )

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
                        .setMessage("Хотите иметь возможность редактировать список совместно с получателем?")
                        .setPositiveButton("Да") { _, _ -> share(offline = false) }
                        .setNegativeButton("Нет") { _, _ -> share(offline = true) }
                        .show()
                    true
                }

                else -> false
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.observeItems(args.whatToBuyListArg).collectLatest { list ->
                    adapter?.submitList(list)
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.badListEventFlow.collect {
                    MaterialAlertDialogBuilder(binding.root.context)
                        .setMessage("Этот список был поврежден или удален. Хотите сохранить его на этом устройстве?")
                        .setPositiveButton("Да") { _, _ ->
                            viewModel.mapListToLocal(args.whatToBuyListArg)
                        }
                        .setNegativeButton("Нет") { _, _ ->
                            viewModel.deleteCurrentList(args.whatToBuyListArg)
                        }
                        .show()
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.onListRemovedEventFlow.collect { findNavController().popBackStack() }
            }
        }
    }

    override fun onPause() {
        viewModel.releaseObservers()
        super.onPause()
    }

    private fun share(offline: Boolean) {
        viewModel.shareList(adapter?.currentList.orEmpty(), requireActivity(), offline)
    }

    override fun onDestroyView() {
        adapter = null
        binding = null
        super.onDestroyView()
    }
}