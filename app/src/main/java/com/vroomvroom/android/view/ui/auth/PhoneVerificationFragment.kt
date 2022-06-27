package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentPhoneVerificationBinding
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PhoneVerificationFragment : BaseFragment<FragmentPhoneVerificationBinding>(
    FragmentPhoneVerificationBinding::inflate
) {

    private var number: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry
        val savedStateHandle = currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>(Constants.SUCCESS)
            ?.observe(currentBackStackEntry) { isCancelled ->
                if (isCancelled) navController.popBackStack()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.toolbar.setupToolbar()

        observeOtpGenerateConfirmation()

        binding.btnGetOtp.setOnClickListener {
            requireActivity().hideSoftKeyboard()
            val editTextValue = binding.phoneNumberEditTxt.text
            if (editTextValue.isNullOrBlank()) {
                showShortToast(getString(R.string.empty_number_message))
                return@setOnClickListener
            }
            number = "+63${editTextValue}"
            authViewModel.registerPhoneNumber(number.orEmpty())
        }
    }

    private fun observeOtpGenerateConfirmation() {
        authViewModel.isOtpSent.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    loadingDialog.show(getString(R.string.loading))
                    binding.btnGetOtp.isEnabled = false
                }
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    binding.btnGetOtp.isEnabled = true
                    authViewModel.resetOtpLiveData()
                    navController.navigate(PhoneVerificationFragmentDirections.
                    actionPhoneVerificationFragmentToCodeVerificationFragment(number.orEmpty()))
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    binding.btnGetOtp.isEnabled = true
                    showShortToast(R.string.invalid_phone_number)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        authViewModel.resetPhoneRegistration()
    }
}