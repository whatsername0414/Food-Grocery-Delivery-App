package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.databinding.FragmentRegisterBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.utils.Utils.isEmailValid
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.utils.Utils.setSafeOnClickListener
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class RegisterFragment : BaseFragment<FragmentRegisterBinding>(
    FragmentRegisterBinding::inflate
) {

    private var currentLocation: LocationEntity? = null
    private var emailAddress:String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()
        prevDestinationId = navController.previousBackStackEntry?.destination?.id ?: -1

        observeLocation()
        observeToken()
        observeNewLogInUser()
        observeRegisterUser()
        observeOtpSent()
        observeVerified()

        binding.txtLogin.setOnClickListener {
            navController.popBackStack()
        }
        binding.btnRegister.setOnClickListener {
            requireActivity().hideSoftKeyboard()
            val password = binding.registerPasswordInputEditText.text?.toString().orEmpty()
            val confirmPassword = binding.registerConfirmPasswordInputEditText.text?.toString()
            if (password == confirmPassword) {
                loadingDialog.show(getString(R.string.loading))
                binding.errorTv.visibility = View.GONE
                authViewModel.registerWithEmailAndPassword(emailAddress, password)
            } else {
                binding.errorTv.visibility = View.VISIBLE
                binding.errorTv.text = getString(R.string.invalid_password)
            }
        }

        binding.btnSendOtp.setSafeOnClickListener {
            requireActivity().hideSoftKeyboard()
            emailAddress = binding.registerEmailInputEditText.text?.toString().orEmpty()
            if (emailAddress.isEmailValid()) {
                loadingDialog.show(getString(R.string.sending))
                authViewModel.generateEmailOtp(emailAddress)
            } else {
                binding.errorTv.visibility = View.VISIBLE
                binding.errorTv.text = getString(R.string.invalid_email_address)
            }
        }

        binding.btnVerifyOtp.setOnClickListener {
            requireActivity().hideSoftKeyboard()
            val otp = binding.registerOtpInputEditText.text?.toString()
            if (otp.isNullOrBlank()) {
                binding.errorTv.text = getString(R.string.otp_invalid)
                return@setOnClickListener
            }
            authViewModel.verifyEmailOtp(emailAddress, otp)
        }
        binding.apply {
            registerEmailInputEditText.doAfterTextChanged {
                registerOtpInputLayout.visibility = View.GONE
                resendTv.visibility = View.GONE
                btnVerifyOtp.visibility = View.GONE
                btnSendOtp.visibility = View.VISIBLE
            }
        }
    }

    private fun observeOtpSent() {
        authViewModel.isOtpSent.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> binding.errorTv.visibility = View.GONE
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    binding.apply {
                        registerOtpInputLayout.isVisible = response.data
                        resendTv.isVisible = response.data
                        btnVerifyOtp.isVisible = response.data
                        btnSendOtp.isVisible = !response.data
                    }

                }
                is Resource.Error -> {
                    binding.errorTv.apply {
                        text = response.exception.message
                        visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun observeVerified() {
        authViewModel.isVerified.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    binding.apply {
                        registerOtpInputLayout.isVisible = !response.data
                        resendTv.isVisible = !response.data
                        btnVerifyOtp.isVisible = !response.data
                        btnSendOtp.isVisible = !response.data
                        btnRegister.isEnabled = response.data
                        registerEmailInputLayout.error = "Verified"
                    }
                }
                is Resource.Error -> {
                    binding.errorTv.text = response.exception.message
                }
            }
        }
    }

    private fun observeLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { locations ->
            currentLocation = locations?.firstOrNull { it.currentUse }
        }
    }

    private fun observeToken() {
        authViewModel.token.observe(viewLifecycleOwner) { token ->
            if (token != null) {
                currentLocation?.let { authViewModel.register(it) }
            }
        }
    }

    private fun observeRegisterUser() {
        authViewModel.isRegistered.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    if (prevDestinationId == R.id.checkoutFragment) {
                        navController.safeNavigate(R.id.action_registerFragment_to_checkoutFragment)
                    } else {
                        navController.safeNavigate(R.id.action_registerFragment_to_homeFragment)
                    }
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    dialog.show(
                        getString(R.string.network_error),
                        getString(R.string.unsaved_error),
                        getString(R.string.cancel),
                        getString(R.string.retry),
                        isButtonLeftVisible = false,
                        isCancellable = false
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                loadingDialog.show(getString(R.string.loading))
                                currentLocation?.let { authViewModel.register(it) }
                                dialog.dismiss()
                            }
                            ClickType.NEGATIVE -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun observeNewLogInUser() {
        authViewModel.newLoggedInUser.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    authViewModel.saveIdToken()
                    binding.errorTv.visibility = View.GONE
                    requireActivity().hideSoftKeyboard()
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    binding.errorTv.visibility = View.VISIBLE
                    binding.errorTv.text = result.exception.message
                }
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.resetOtpLiveData()
    }
}