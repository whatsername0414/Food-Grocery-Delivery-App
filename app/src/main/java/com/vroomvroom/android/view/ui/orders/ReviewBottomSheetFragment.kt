package com.vroomvroom.android.view.ui.orders

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentReviewBottomSheetBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import com.vroomvroom.android.view.ui.common.CommonAlertDialog
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
            val comment = binding.inputEditText.text?.toString().orEmpty()
            ordersViewModel.createReview(args.orderId, args.merchantId, rate, comment)
        }
    }

    private fun observeReviewLiveData() {
        ordersViewModel.isReviewCreated.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    loadingDialog.show(getString(R.string.loading))
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                }
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    mainActivityViewModel.reviewed.postValue(true)
                    dismiss()
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    isCancelable = true
                    dialog.show(
                        getString(R.string.failed),
                        response.exception.message ?: getString(R.string.general_error_message),
                        getString(R.string.cancel),
                        getString(R.string.retry)
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                val rate = binding.ratingBar.rating.toInt()
                                val comment = binding.inputEditText.text?.toString().orEmpty()
                                ordersViewModel.createReview(args.orderId, args.merchantId, rate, comment)
                                dialog.dismiss()
                            }
                            ClickType.NEGATIVE -> dialog.dismiss()
                        }
                    }
                }
            }
        }
    }

}