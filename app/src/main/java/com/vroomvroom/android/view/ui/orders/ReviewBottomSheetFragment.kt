package com.vroomvroom.android.view.ui.orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.databinding.FragmentReviewBottomSheetBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.activityviewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.orders.viewmodel.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ReviewBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentReviewBottomSheetBinding

    private val viewModel by viewModels<OrdersViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val args: ReviewBottomSheetFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReviewBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeReviewLiveData()

        binding.btnSubmit.setOnClickListener {
            val rate = binding.ratingBar.rating.toInt()
            val review = binding.inputEditText.text?.toString()
            viewModel.mutationReview(args.merchantId, args.orderId, rate, review)
        }
    }

    private fun observeReviewLiveData() {
        viewModel.review.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Loading -> {
                    isCancelable = false
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                }
                is ViewState.Success -> {
                    activityViewModel.reviewed.postValue(true)
                    dismiss()
                }
                is ViewState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    isCancelable = true
                }
            }
        })
    }

}