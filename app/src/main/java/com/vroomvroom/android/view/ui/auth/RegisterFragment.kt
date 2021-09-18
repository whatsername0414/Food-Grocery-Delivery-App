package com.vroomvroom.android.view.ui.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.annotation.Nullable
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentRegisterBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.main.HomeActivity
import com.vroomvroom.android.view.ui.startNewActivity
import com.vroomvroom.android.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<AuthViewModel>()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        binding.progressbar.visibility = View.GONE
        binding.registerConnectionFailedNotice.visibility = View.GONE

        binding.registerButton.setOnClickListener {
            register()
        }
        binding.registerRetryButton.setOnClickListener {
            retry()
        }

        binding.emailInputEditText.doOnTextChanged { text, _, _, _ ->
                if (!Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches() ) {
                    binding.emailInputLayout.helperText = "Invalid email address"
                    binding.emailInputLayout.isHelperTextEnabled = true
                    if (text!!.isEmpty()) {
                        binding.emailInputLayout.helperText = "Email must not be empty"
                    }
                } else {
                    binding.emailInputLayout.isHelperTextEnabled = false
                }

        }
        binding.registerPasswordInputEditText.doOnTextChanged { text, _, _, _ ->
                if (text!!.isEmpty()) {
                    binding.registerPasswordInputLayout.helperText = "Password must not be empty"
                } else {
                    binding.registerPasswordInputLayout.isHelperTextEnabled = false
                }

        }
        binding.registerConfirmPasswordInputEditText.doOnTextChanged { text, _, _, _ ->
            if (text.contentEquals(binding.registerConfirmPasswordInputEditText.text)) {
                binding.registerConfirmPasswordInputLayout.boxStrokeColor =
                    resources.getColor(R.color.match, requireContext().theme)
                binding.registerConfirmPasswordInputLayout.isHelperTextEnabled = false
            } else {
                binding.registerConfirmPasswordInputLayout.helperText = "Password must match"
                binding.registerConfirmPasswordInputLayout.boxStrokeColor =
                    resources.getColor(R.color.maroon, requireContext().theme)
            }
        }

        binding.txLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment)
            binding.registerButton.isEnabled = false
        }
    }

    private fun register() {
        val email = binding.emailInputEditText.text.toString()
        val password = binding.registerPasswordInputEditText.text.toString()
        val confirmPassword = binding.registerConfirmPasswordInputEditText.text.toString()

        when {
            email == "" -> {
                binding.emailInputLayout.helperText = "Email must not be empty"
            }
            password == "" -> {
                binding.registerPasswordInputLayout.helperText = "Password must not be empty"
            }
            confirmPassword == "" -> {
                binding.registerConfirmPasswordInputLayout.helperText = "Password must match"
            }
            else -> {
                viewModel.mutationRegister(email, password, confirmPassword)
                observeLiveData()
            }
        }
    }

    private fun retry() {
        binding.registerLinearLayout.visibility = View.VISIBLE
        binding.registerConnectionFailedNotice.visibility = View.GONE
        binding.registerButton.isEnabled = true
    }

    private fun observeLiveData() {
        viewModel.registerToken.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.registerLinearLayout.visibility = View.VISIBLE
                    binding.registerConnectionFailedNotice.visibility = View.GONE
                    binding.progressbar.visibility = View.VISIBLE
                    binding.registerButton.isEnabled = false
                }
                is ViewState.Success -> {
                    if (response.value?.data?.register == null) {
                        binding.progressbar.visibility = View.GONE
                    } else {
                        requireActivity().startNewActivity(HomeActivity::class.java)
                    }
                    binding.progressbar.visibility = View.GONE
                }
                is ViewState.Error -> {
                    binding.registerLinearLayout.visibility = View.GONE
                    binding.registerEmptyTextTitle.visibility = View.VISIBLE
                    binding.registerConnectionFailedNotice.visibility = View.VISIBLE
                    binding.progressbar.visibility = View.GONE
                }
                is ViewState.Auth -> {
                    if (response.message == "Email is taken") {
                        binding.emailInputLayout.helperText = response.message
                    }
                    binding.progressbar.visibility = View.GONE
                }
            }
        }
    }
}