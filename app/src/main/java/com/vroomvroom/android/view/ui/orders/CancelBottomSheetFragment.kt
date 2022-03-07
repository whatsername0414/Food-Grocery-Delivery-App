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
import com.vroomvroom.android.utils.Constants.CANCEL_SUCCESSFUL
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import com.vroomvroom.android.view.ui.widget.CommonAlertDialog
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
        ordersViewModel.cancelled.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.btnConfirm.isEnabled = false
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    showShortToast(R.string.cancel_successful)
                    mainActivityViewModel.isRefreshed.postValue(true)
                    savedStateHandle[CANCEL_SUCCESSFUL] = true
                    findNavController().popBackStack()
                }
                is ViewState.Error -> {
                    isCancelable = true
                    binding.btnConfirm.isEnabled = true
                    binding.progressIndicator.visibility = View.GONE
                    initAlertDialog()
                }
            }
        }
    }

    private fun initAlertDialog() {
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
        ordersViewModel.mutationCancelOrder(args.orderId, reason)
    }
}