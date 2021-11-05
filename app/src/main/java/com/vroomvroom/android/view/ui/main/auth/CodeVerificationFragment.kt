package com.vroomvroom.android.view.ui.main.auth

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCodeVerificationBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.regex.Pattern

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CodeVerificationFragment : Fragment() {

    private val viewModel by activityViewModels<AuthViewModel>()
    private lateinit var binding: FragmentCodeVerificationBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCodeVerificationBinding.inflate(inflater)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.otpVerificationProgress.visibility = View.GONE

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.otpVerificationToolbar.setupWithNavController(navController, appBarConfiguration)

        observeMessageIntent()
        observeUpdatedUser()
        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.otpEditTxt.text.toString()
            if (otp.isNotBlank()) {
                viewModel.mutationOtpVerification(otp)
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
        viewModel.messageIntent.observe(viewLifecycleOwner, { intent ->
            when (intent) {
                is ViewState.Success -> getResult.launch(intent.result)
                is ViewState.Error -> {
                    Toast.makeText(
                        requireContext(), intent.exception.message, Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Log.d("PhoneVerificationFragment", intent.toString())
            }
        })
    }

    private fun observeUpdatedUser() {
        viewModel.updatedUser.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.otpVerificationProgress.visibility = View.VISIBLE
                    binding.btnVerifyOtp.isEnabled = false
                }
                is ViewState.Success -> {
                    navController.navigate(R.id.action_codeVerificationFragment_to_checkoutFragment)
                }
                is ViewState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        response.exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun getOtpFromMessage(message: String) {
        val otpPattern = Pattern.compile("(|^)\\d{6}")
        val matcher = otpPattern.matcher(message)
        if (matcher.find()) {
            binding.otpEditTxt.setText(matcher.group())
        }
    }

}