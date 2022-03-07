package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLoginBinding
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.utils.Utils.isEmailValid
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginProgress.visibility = View.GONE
        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()

        observeNewLoggedInUser()

        binding.forgotPassword.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_passwordResetFragment)
        }

        binding.txtSignUp.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            requireActivity().hideSoftKeyboard()
            binding.loginEmailInputLayout.isHelperTextEnabled = false
            binding.loginPasswordInputLayout.isHelperTextEnabled = false
            val emailAddress = binding.loginEmailInputEditText.text.toString()
            val password = binding.loginPasswordInputEditText.text.toString()
            if (emailAddress.isEmailValid()) {
                binding.loginProgress.visibility = View.VISIBLE
                authViewModel.logInWithEmailAndPassword(emailAddress, password)
            } else binding.loginEmailInputLayout.helperText = "Invalid email"
        }
    }

    private fun observeNewLoggedInUser() {
        authViewModel.newLoggedInUser.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ViewState.Success -> {
                    requireActivity().hideSoftKeyboard()
                    navController.popBackStack()
                }
                is ViewState.Error -> {
                    binding.loginProgress.visibility = View.GONE
                    binding.loginPasswordInputLayout.isHelperTextEnabled = true
                    binding.loginEmailInputLayout.isHelperTextEnabled = true
                }
                else -> Unit
            }
        }
    }
}