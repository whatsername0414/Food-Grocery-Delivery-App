package com.vroomvroom.android.view.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.databinding.FragmentCancelBottomSheetBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.activityviewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.orders.viewmodel.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CancelBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel by viewModels<OrdersViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val args: CancelBottomSheetFragmentArgs by navArgs()

    private lateinit var binding: FragmentCancelBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCancelBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeCancelled()

        binding.btnConfirm.setOnClickListener {
            isCancelable = false
            val checkRbId = binding.radioGroup.checkedRadioButtonId
            val reason = view.findViewById<RadioButton>(checkRbId).text.toString()
            viewModel.mutationCancelOrder(args.orderId, reason)
        }
    }

    private fun observeCancelled() {
        viewModel.cancelled.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.btnConfirm.isEnabled = false
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    Toast.makeText(requireContext(), response.result.cancelOrder, Toast.LENGTH_SHORT).show()
                    activityViewModel.isRefreshed.postValue(true)
                    dismiss()
                }
                is ViewState.Error -> {
                    binding.btnConfirm.isEnabled = true
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}