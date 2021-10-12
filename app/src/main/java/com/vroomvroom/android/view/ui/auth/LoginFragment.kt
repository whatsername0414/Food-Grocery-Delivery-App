package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLoginBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.main.HomeActivity
import com.vroomvroom.android.view.ui.Utils.startNewActivity
import com.vroomvroom.android.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<AuthViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.progressbar.visibility = View.GONE
        binding.loginConnectionFailedNotice.visibility = View.GONE

        binding.loginButton.setOnClickListener {
            login()
        }
        binding.loginRetryButton.setOnClickListener {
            retry()
        }

        binding.txSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment)
        }

        binding.loginEmailInputEditText.doOnTextChanged { text, _, _, _ ->
            if (text!!.isEmpty()) {
                binding.loginEmailInputLayout.helperText = "Email must not be empty"
            } else binding.loginEmailInputLayout.isHelperTextEnabled = false
        }
        binding.loginPasswordInputEditText.doOnTextChanged { text, _, _, _ ->
            if (text!!.isEmpty()) {
                binding.loginPasswordInputLayout.helperText = "Password must not be empty"
            } else binding.loginPasswordInputLayout.isHelperTextEnabled = false
        }
    }

    private fun retry() {
        binding.loginLinearLayout.visibility = View.VISIBLE
        binding.loginConnectionFailedNotice.visibility = View.GONE
        binding.loginButton.isEnabled = true
    }

    private fun login() {
        val email = binding.loginEmailInputEditText.text.toString()
        val password = binding.loginPasswordInputEditText.text.toString()

        when {
            email == "" -> {
                binding.loginEmailInputLayout.helperText = "Email must not be empty"
            }
            password == "" -> {
                binding.loginPasswordInputLayout.helperText = "Password must not be empty"
            }
            else -> {
                viewModel.mutationLogin(email, password)
                observeLiveData()
            }
        }
    }

    private fun observeLiveData() {
        viewModel.loginToken.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.loginConnectionFailedNotice.visibility = View.GONE
                    binding.loginLinearLayout.visibility = View.VISIBLE
                    binding.progressbar.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                }
                is ViewState.Success -> {
                    if (response.value?.data?.login == null) {
                        binding.progressbar.visibility = View.GONE
                    } else {
                        requireActivity().startNewActivity(HomeActivity::class.java)
                    }
                    binding.progressbar.visibility = View.GONE
                }
                is ViewState.Error -> {
                    binding.loginEmptyTextTitle.visibility = View.VISIBLE
                    binding.loginConnectionFailedNotice.visibility = View.VISIBLE
                    binding.progressbar.visibility = View.GONE
                    binding.loginLinearLayout.visibility = View.GONE
                }
                is ViewState.Auth -> {
                    if (response.message.toString() == "User not found") {
                        binding.loginEmailInputLayout.helperText = response.message.toString()
                        binding.loginEmailInputLayout.isHelperTextEnabled = false
                    }
                    if (response.message.toString() == "Wrong credentials"){
                        binding.loginPasswordInputLayout.helperText = response.message.toString()
                        binding.loginPasswordInputLayout.isHelperTextEnabled = false
                    }
                    binding.progressbar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                }
            }
        }
    }
}