package com.aleksandrovych.purchasepal.lists.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.aleksandrovych.purchasepal.KeyboardManager
import com.aleksandrovych.purchasepal.R
import com.aleksandrovych.purchasepal.databinding.DialogAddWhatToByListBinding
import com.aleksandrovych.purchasepal.extensions.launchWhenResumed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddWhatToBuyListDialog : DialogFragment() {

    @[Inject Suppress("ProtectedInFinal")]
    protected lateinit var keyboardManager: KeyboardManager

    private val viewModel: AddWhatToBuyListViewModel by viewModels()
    private var binding: DialogAddWhatToByListBinding? = null
    private val args: AddWhatToBuyListDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogAddWhatToByListBinding.inflate(
            inflater,
            container,
            false,
        ).apply(::onViewCreated)
        return binding!!.root
    }

    private fun onViewCreated(binding: DialogAddWhatToByListBinding) {
        binding.titleEditText.setText(args.whatToBuyListPrefillArg.title)
        binding.titleEditText.setSelection(args.whatToBuyListPrefillArg.title.length)

        lifecycleScope.launch {
            delay(300)
            keyboardManager.show(binding.titleEditText)
        }

        binding.saveButton.setOnClickListener {
            if (binding.titleEditText.text?.toString().isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    R.string.message_not_all_fields_are_filled_in,
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }
            viewModel.saveList(binding.titleEditText.text.toString())
        }

        launchWhenResumed {
            viewModel.dismissFlow.collect { dismiss() }
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