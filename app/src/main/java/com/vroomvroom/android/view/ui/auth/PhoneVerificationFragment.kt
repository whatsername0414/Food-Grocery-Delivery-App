package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentPhoneVerificationBinding
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PhoneVerificationFragment : BaseFragment<FragmentPhoneVerificationBinding>(
    FragmentPhoneVerificationBinding::inflate
) {

    private var isNavigated = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.phoneVerificationProgress.visibility = View.GONE

        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()

        observeOtpGenerateConfirmation()

        binding.btnGetOtp.setOnClickListener {
            requireActivity().hideSoftKeyboard()
            val editTextValue = binding.phoneNumberEditTxt.text.toString()
            val number = "+63${editTextValue}"
            authViewModel.mutationVerifyMobileNumber(number)
            authViewModel.registerBroadcastReceiver()
        }
    }

    private fun observeOtpGenerateConfirmation() {
        authViewModel.otpGenerateConfirmation.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.phoneVerificationProgress.visibility = View.VISIBLE
                    binding.btnGetOtp.isEnabled = false
                }
                is ViewState.Success -> {
                    binding.phoneVerificationProgress.visibility = View.GONE
                    binding.btnGetOtp.isEnabled = true
                    isNavigated = true
                    if (!isNavigated) {
                        navController.navigate(R.id.action_phoneVerificationFragment_to_codeVerificationFragment)
                    }
                }
                is ViewState.Error -> {
                    binding.phoneVerificationProgress.visibility = View.GONE
                    binding.btnGetOtp.isEnabled = true
                    showShortToast(R.string.invalid_phone_number)
                }
            }
        }
    }
}