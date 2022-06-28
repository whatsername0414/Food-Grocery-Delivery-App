package com.vroomvroom.android.view.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCodeVerificationBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.regex.Pattern

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CodeVerificationFragment : BaseFragment<FragmentCodeVerificationBinding>(
    FragmentCodeVerificationBinding::inflate
) {
    private val args: CodeVerificationFragmentArgs by navArgs()
    private lateinit var savedStateHandle: SavedStateHandle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()
        navController.previousBackStackEntry?.savedStateHandle?.let {
            savedStateHandle = it
        }
        prevDestinationId = navController.previousBackStackEntry?.destination?.id ?: -1

        authViewModel.registerBroadcastReceiver()
        observeMessageIntent()
        observeOtpVerificationResult()
        binding.btnVerifyOtp.setOnClickListener {
            verifyOtp()
        }

        binding.resend.setOnClickListener {
            dialog.show(
                title = getString(R.string.prompt),
                message = getString(R.string.confirm_number),
                leftButtonTitle = getString(R.string.change),
                rightButtonTitle = getString(R.string.confirm)
            ) { type ->
                when (type) {
                    ClickType.POSITIVE -> {
                        dialog.dismiss()
                        authViewModel.registerPhoneNumber(args.number)
                    }
                    ClickType.NEGATIVE -> {
                        dialog.dismiss()
                        navController.popBackStack()
                    }
                }
            }
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
                is Resource.Success -> getResult.launch(intent.data)
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(), intent.exception.message, Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Unit
            }
        }
    }

    private fun observeOtpVerificationResult() {
        authViewModel.isVerified.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    loadingDialog.show(getString(R.string.loading))
                    binding.btnVerifyOtp.isEnabled = false
                }
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    initDialog(
                        getString(R.string.congratulations),
                        getString(R.string.verified_phone_message),
                        getString(R.string.ok),
                        getString(R.string.ok)
                    )
                    if (prevDestinationId == R.id.checkoutFragment) {
                        navController.safeNavigate(R.id.action_codeVerificationFragment_to_checkoutFragment)
                    } else {
                        savedStateHandle[Constants.SUCCESS] = true
                        navController.popBackStack()
                    }
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    binding.btnVerifyOtp.isEnabled = true
                    initDialog(
                        getString(R.string.verification_failed),
                        response.exception.message.orEmpty(),
                        getString(R.string.ok),
                        getString(R.string.retry)
                    )
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
            authViewModel.verifyPhoneOtp(otp)
        }
    }

    private fun initDialog(
        title: String,
        message: String,
        leftButtonTitle: String,
        rightButtonTitle: String
    ) {
        dialog.show(
            title = title,
            message = message,
            leftButtonTitle = leftButtonTitle,
            rightButtonTitle = rightButtonTitle,
            isButtonLeftVisible = false,
            isCancellable = false
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> dialog.dismiss()
                ClickType.NEGATIVE -> dialog.dismiss()
            }
        }
    }

}