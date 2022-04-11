package com.vroomvroom.android.view.ui.orders

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentReviewBottomSheetBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import com.vroomvroom.android.view.ui.widget.CommonAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ReviewBottomSheetFragment : BaseBottomSheetFragment<FragmentReviewBottomSheetBinding>(
    FragmentReviewBottomSheetBinding::inflate
) {

    private val args: ReviewBottomSheetFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeReviewLiveData()

        binding.btnSubmit.setOnClickListener {
            val rate = binding.ratingBar.rating.toInt()
            val review = binding.inputEditText.text?.toString()
            ordersViewModel.mutationReview(args.merchantId, args.orderId, rate, review)
        }
    }

    private fun observeReviewLiveData() {
        ordersViewModel.review.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> {
                    isCancelable = false
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                }
                is ViewState.Success -> {
                    mainActivityViewModel.reviewed.postValue(true)
                    dismiss()
                }
                is ViewState.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    isCancelable = true
                    initAlertDialog()
                }
            }
        }
    }

    private fun initAlertDialog() {
        val dialog = CommonAlertDialog(
            requireActivity()
        )
        dialog.show(
            getString(R.string.network_error),
            getString(R.string.network_error_message),
            getString(R.string.cancel),
            getString(R.string.retry)
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    val rate = binding.ratingBar.rating.toInt()
                    val review = binding.inputEditText.text?.toString()
                    ordersViewModel.mutationReview(args.merchantId, args.orderId, rate, review)
                    dialog.dismiss()
                }
                ClickType.NEGATIVE -> dialog.dismiss()
            }
        }
    }

}