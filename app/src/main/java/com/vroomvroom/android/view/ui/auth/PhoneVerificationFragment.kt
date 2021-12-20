package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentPhoneVerificationBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PhoneVerificationFragment : Fragment() {

    private val viewModel by activityViewModels<AuthViewModel>()
    private lateinit var binding: FragmentPhoneVerificationBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhoneVerificationBinding.inflate(inflater)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.phoneVerificationProgress.visibility = View.GONE

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.phoneVerificationToolbar.setupWithNavController(navController, appBarConfiguration)

        observeOtpGenerateConfirmation()

        binding.btnGetOtp.setOnClickListener {
            val editTextValue = binding.phoneNumberEditTxt.text.toString()
            val number = "+63${editTextValue}"
            viewModel.mutationVerifyMobileNumber(number)
            viewModel.registerBroadcastReceiver()
        }
    }

    private fun observeOtpGenerateConfirmation() {
        viewModel.otpGenerateConfirmation.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.phoneVerificationProgress.visibility = View.VISIBLE
                    binding.btnGetOtp.isEnabled = false
                }
                is ViewState.Success -> {
                    binding.phoneVerificationProgress.visibility = View.GONE
                    binding.btnGetOtp.isEnabled = true
                    navController.navigate(R.id.action_phoneVerificationFragment_to_codeVerificationFragment)
                }
                is ViewState.Error -> {
                    binding.phoneVerificationProgress.visibility = View.GONE
                    binding.btnGetOtp.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Invalid phone number",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}