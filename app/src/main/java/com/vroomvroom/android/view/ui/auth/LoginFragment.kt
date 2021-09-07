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
import com.vroomvroom.android.view.ui.startNewActivity
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
        binding.buttonLogin.setOnClickListener {
            login()
        }
        binding.txSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment)
        }

        binding.usernameInputEditText.doOnTextChanged { text, _, _, _ ->
            if (text!!.isEmpty()) {
                binding.usernameInputLayout.helperText = "Username must not be empty"
            } else binding.usernameInputLayout.isHelperTextEnabled = false
        }
        binding.passwordInputEditText.doOnTextChanged { text, _, _, _ ->
            if (text!!.isEmpty()) {
                binding.passwordInputLayout.helperText = "Password must not be empty"
            } else binding.passwordInputLayout.isHelperTextEnabled = false
        }
    }

    private fun login() {
        val username = binding.usernameInputEditText.text.toString()
        val password = binding.passwordInputEditText.text.toString()

        when {
            username == "" -> {
                binding.usernameInputLayout.helperText = "Username must not be empty"
            }
            password == "" -> {
                binding.passwordInputLayout.helperText = "Password must not be empty"
            }
            else -> {
                viewModel.mutationLogin(username, password)
                observeLiveData()
            }
        }
    }

    private fun observeLiveData() {
        viewModel.loginToken.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.progressbar.visibility = View.VISIBLE
                    binding.buttonLogin.isEnabled = false
                }
                is ViewState.Success -> {
                    println(response.value?.data?.login)
                    if (response.value?.data?.login == null) {
                        binding.progressbar.visibility = View.GONE
                    } else {
                        requireActivity().startNewActivity(HomeActivity::class.java)
                    }
                    binding.progressbar.visibility = View.GONE
                }
                is ViewState.Error -> {
                    binding.loginEmptyText.visibility = View.VISIBLE
                    binding.progressbar.visibility = View.GONE
                }
                is ViewState.Auth -> {
                    if (response.message.toString() == "User not found") {
                        binding.usernameInputLayout.helperText = response.message.toString()
                        binding.passwordInputLayout.isHelperTextEnabled = false
                    }
                    if (response.message.toString() == "Wrong credentials"){
                        binding.passwordInputLayout.helperText = response.message.toString()
                        binding.usernameInputLayout.isHelperTextEnabled = false
                    }
                    binding.progressbar.visibility = View.GONE
                    binding.buttonLogin.isEnabled = true
                }
            }
        }
    }
}