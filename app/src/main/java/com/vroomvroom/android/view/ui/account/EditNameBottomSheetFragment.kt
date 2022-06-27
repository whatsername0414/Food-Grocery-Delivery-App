package com.vroomvroom.android.view.ui.account

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentEditNameBottomSheetBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditNameBottomSheetFragment : BaseBottomSheetFragment<FragmentEditNameBottomSheetBinding>(
    FragmentEditNameBottomSheetBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUpdateName()

        binding.btnSave.setOnClickListener {
            val name = binding.nameInputEditText.text.toString()
            if (name.isBlank()) {
                showShortToast(R.string.empty_name)
                return@setOnClickListener
            }
            accountViewModel.updateName(name)
        }
    }

    private fun observeUpdateName() {
        accountViewModel.isNameUpdated.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnCancel.isEnabled = false
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.btnCancel.isEnabled = true
                    binding.progressIndicator.visibility = View.GONE
                    initAlertDialog()
                }
            }
        }
    }

    private fun initAlertDialog() {
        dialog.show(
            getString(R.string.network_error),
            getString(R.string.network_error_message),
            getString(R.string.cancel),
            getString(R.string.retry)
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    val name = binding.nameInputEditText.text
                    if (name.toString().filter { !it.isWhitespace() }.isNotBlank()) {
                        accountViewModel.updateName(name.toString())
                    }
                    dialog.dismiss()
                }
                ClickType.NEGATIVE -> dialog.dismiss()
            }
        }
    }
}