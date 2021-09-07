package com.vroomvroom.android.view.ui.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
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
        binding.buttonRegister.setOnClickListener {
            register()
        }

        binding.usernameInputEditText.doOnTextChanged { text, _, _, _ ->
                if (text!!.isEmpty()) {
                    binding.usernameInputLayout.helperText = "Username must not be empty"
                } else binding.usernameInputLayout.isHelperTextEnabled = false
        }

        binding.emailInputEditText.doOnTextChanged { text, _, _, _ ->
                if (text!!.isEmpty()) {
                    binding.emailInputLayout.helperText = "Email must not be empty"
                } else binding.emailInputLayout.isHelperTextEnabled = false
        }
        binding.passwordInputEditText.doOnTextChanged { text, _, _, _ ->
                if (text!!.isEmpty()) {
                    binding.passwordInputLayout.helperText = "Password must not be empty"
                } else binding.passwordInputLayout.isHelperTextEnabled = false
        }
        binding.confirmPasswordInputEditText.doOnTextChanged { text, _, _, _ ->
            if (text.contentEquals(binding.passwordInputEditText.text)) {
                binding.confirmPasswordInputLayout.boxStrokeColor =
                    resources.getColor(R.color.match, requireContext().theme)
                binding.confirmPasswordInputLayout.isHelperTextEnabled = false
            } else {
                binding.confirmPasswordInputLayout.helperText = "Password must match"
                binding.confirmPasswordInputLayout.boxStrokeColor =
                    resources.getColor(R.color.maroon, requireContext().theme)
            }
        }

        binding.txLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment)
            binding.buttonRegister.isEnabled = false
        }
    }

    private fun register() {
        val username = binding.usernameInputEditText.text.toString()
        val email = binding.emailInputEditText.text.toString()
        val password = binding.passwordInputEditText.text.toString()
        val confirmPassword = binding.confirmPasswordInputEditText.text.toString()

        when {
            username == "" -> {
                binding.usernameInputLayout.helperText = "Username must not be empty"
            }
            email == "" -> {
                binding.emailInputLayout.helperText = "Email must not be empty"
            }
            password == "" -> {
                binding.passwordInputLayout.helperText = "Password must not be empty"
            }
            confirmPassword == "" -> {
                binding.confirmPasswordInputLayout.helperText = "Password must match"
            }
            else -> {
                viewModel.mutationLogin(username, password)
                observeLiveData()
            }
        }

        viewModel.mutationRegister(username, email, password, confirmPassword)
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.registerToken.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.progressbar.visibility = View.VISIBLE
                    binding.buttonRegister.isEnabled = false
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
                    binding.registerEmptyText.visibility = View.VISIBLE
                    binding.progressbar.visibility = View.GONE
                }
                is ViewState.Auth -> {
                    if (response.message == "Username is taken") {
                        binding.usernameInputLayout.helperText = response.message
                    }
                    binding.progressbar.visibility = View.GONE
                }
            }
        }
    }
}