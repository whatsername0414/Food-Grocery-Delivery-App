package com.vroomvroom.android.view.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCodeVerificationBinding
import com.vroomvroom.android.domain.db.user.Phone
import com.vroomvroom.android.domain.db.user.UserEntity
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.widget.CommonAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.regex.Pattern

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CodeVerificationFragment : BaseFragment<FragmentCodeVerificationBinding>(
    FragmentCodeVerificationBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.otpVerificationProgress.visibility = View.GONE
        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()

        observeMessageIntent()
        observeOtpVerificationResult()
        binding.btnVerifyOtp.setOnClickListener {
            verifyOtp()
        }
    }

    private fun observeMessageIntent() {
        val getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                message?.let {
                    getOtpFromMessage(it)
                }
            }
        }
        authViewModel.messageIntent.observe(viewLifecycleOwner) { intent ->
            when (intent) {
                is ViewState.Success -> getResult.launch(intent.result)
                is ViewState.Error -> {
                    Toast.makeText(
                        requireContext(), intent.exception.message, Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Unit
            }
        }
    }

    private fun observeOtpVerificationResult() {
        authViewModel.otpVerificationResult.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.otpVerificationProgress.visibility = View.VISIBLE
                    binding.btnVerifyOtp.isEnabled = false
                }
                is ViewState.Success -> {
                    val result = response.result.otpVerification
                    val user = UserEntity(
                        result.id,
                        result.name,
                        result.email,
                        Phone(result.phone?.number, result.phone?.verified!!)
                    )
                    authViewModel.updateUserRecord(user)
                    navController.navigate(R.id.action_codeVerificationFragment_to_checkoutFragment)
                }
                is ViewState.Error -> {
                    binding.otpVerificationProgress.visibility = View.GONE
                    binding.btnVerifyOtp.isEnabled = true
                    if (response.exception.message == "Bad Request") {
                        initAlertDialog(
                            getString(R.string.verification_error),
                            getString(R.string.verification_error_message)
                        )
                    } else {
                        initAlertDialog(
                            getString(R.string.network_error),
                            getString(R.string.network_error_message)
                        )
                    }
                }
            }
        }
    }

    private fun getOtpFromMessage(message: String) {
        val otpPattern = Pattern.compile("(|^)\\d{6}")
        val matcher = otpPattern.matcher(message)
        if (matcher.find()) {
            binding.otpEditTxt.setText(matcher.group())
            verifyOtp()
        }
    }

    private fun verifyOtp() {
        requireActivity().hideSoftKeyboard()
        val otp = binding.otpEditTxt.text.toString()
        if (otp.isNotBlank()) {
            authViewModel.mutationOtpVerification(otp)
        }
    }

    private fun initAlertDialog(title: String, message: String) {
        val dialog = CommonAlertDialog(
            requireActivity()
        )
        dialog.show(
            title,
            message,
            getString(R.string.cancel),
            getString(R.string.retry),
            false
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> dialog.dismiss()
                ClickType.NEGATIVE -> Unit
            }
        }
    }

}