package com.vroomvroom.android.view.ui.orders

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCancelBottomSheetBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Constants.SUCCESS
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import com.vroomvroom.android.view.ui.common.CommonAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CancelBottomSheetFragment : BaseBottomSheetFragment<FragmentCancelBottomSheetBinding>(
    FragmentCancelBottomSheetBinding::inflate
) {

    private val args: CancelBottomSheetFragmentArgs by navArgs()
    private lateinit var savedStateHandle: SavedStateHandle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().previousBackStackEntry?.savedStateHandle?.let {
            savedStateHandle = it
        }

        observeCancelled()

        binding.btnConfirm.setOnClickListener {
            submitReason()
        }
    }

    private fun observeCancelled() {
        ordersViewModel.isCancelled.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    binding.btnConfirm.isEnabled = false
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    handleSuccess()
                }
                is Resource.Error -> {
                    isCancelable = true
                    binding.btnConfirm.isEnabled = true
                    binding.progressIndicator.visibility = View.GONE
                    handleFailed()
                }
            }
        }
    }

    private fun handleSuccess() {
        mainActivityViewModel.isRefreshed.postValue(true)
        savedStateHandle[SUCCESS] = true
        val dialog = CommonAlertDialog(requireActivity())
        dialog.show(
            getString(R.string.cancelled_title),
            getString(R.string.cancelled_message),
            getString(R.string.ok),
            getString(R.string.ok),
            false
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    dialog.dismiss()
                }
                ClickType.NEGATIVE -> Unit
            }
        }
    }

    private fun handleFailed() {
        val dialog = CommonAlertDialog(requireActivity())
        dialog.show(
            getString(R.string.network_error),
            getString(R.string.network_error_message),
            getString(R.string.cancel),
            getString(R.string.retry)
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    submitReason()
                    dialog.dismiss()
                }
                ClickType.NEGATIVE -> dialog.dismiss()
            }
        }
    }

    private fun submitReason() {
        isCancelable = false
        val checkRbId = binding.radioGroup.checkedRadioButtonId
        val reason = binding.root.findViewById<RadioButton>(checkRbId).text.toString()
        ordersViewModel.cancelOrder(args.orderId, reason)
    }
}