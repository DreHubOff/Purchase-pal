package com.aleksandrovych.purchasepal.whatToBuy.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.aleksandrovych.purchasepal.KeyboardManager
import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.databinding.DialogAddWhatToByBinding
import com.aleksandrovych.purchasepal.extensions.launchWhenResumed
import com.aleksandrovych.purchasepal.whatToBuy.WhatToBuy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddWhatToBuyDialog : DialogFragment() {

    @Inject
    @Suppress("ProtectedInFinal")
    protected lateinit var keyboardManager: KeyboardManager

    private val viewModel: AddWhatToBuyViewModel by viewModels()
    private var binding: DialogAddWhatToByBinding? = null
    private val args: AddWhatToBuyDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogAddWhatToByBinding.inflate(layoutInflater, container, false)
        onViewCreated(binding!!)
        return binding!!.root
    }

    private fun onViewCreated(binding: DialogAddWhatToByBinding) {
        binding
            .positionInShopInputEditText
            .setText(args.positionPrefillArg.toString())

        binding.saveButton.setOnClickListener {
            val title = binding.titleAutoCompleteTextView.text?.toString().orEmpty().ifEmpty {
                Toast.makeText(
                    binding.root.context,
                    R.string.message_not_all_fields_are_filled_in,
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }
            val positionInShop = binding
                .positionInShopInputEditText
                .text
                ?.toString()
                .orEmpty()
                .runCatching(String::toInt)
                .getOrElse { 0 }
                .coerceAtLeast(0)
            val item = WhatToBuy(
                title = title,
                orderInShop = positionInShop,
                listId = args.listIdArg
            )
            binding.saveButton.isEnabled = false
            binding.progressIndicator.isGone = false
            viewModel.saveNewItem(item, ::dismiss)
        }

        val suggestionsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>(),
        )
        binding.titleAutoCompleteTextView.setAdapter(suggestionsAdapter)

        binding.titleAutoCompleteTextView.doOnTextChanged { text, _, _, _ ->
            viewModel.requestSuggestions(text?.toString())
        }

        lifecycleScope.launch {
            delay(300)
            keyboardManager.show(binding.titleAutoCompleteTextView)
        }

        launchWhenResumed {
            viewModel.suggestionsFlow.collectLatest { suggestions ->
                suggestionsAdapter.clear()
                suggestionsAdapter.addAll(suggestions)
                suggestionsAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}