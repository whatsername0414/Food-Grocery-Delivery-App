package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLoginBinding
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.utils.Utils.isEmailValid
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel by viewModels<AuthViewModel>()

    private lateinit var navController: NavController
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginProgress.visibility = View.GONE
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.loginToolbar.setupWithNavController(navController, appBarConfiguration)

        observeNewLoggedInUser()

        binding.forgotPassword.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_passwordResetFragment)
        }

        binding.txtSignUp.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            binding.loginEmailInputLayout.isHelperTextEnabled = false
            binding.loginPasswordInputLayout.isHelperTextEnabled = false
            val emailAddress = binding.loginEmailInputEditText.text.toString()
            val password = binding.loginPasswordInputEditText.text.toString()
            if (emailAddress.isEmailValid()) {
                binding.loginProgress.visibility = View.VISIBLE
                viewModel.logInWithEmailAndPassword(emailAddress, password)
            } else binding.loginEmailInputLayout.helperText = "Invalid email"
        }
    }

    private fun observeNewLoggedInUser() {
        viewModel.newLoggedInUser.observe(viewLifecycleOwner, { result ->
            when (result) {
                is ViewState.Success -> {
                    requireActivity().hideSoftKeyboard()
                    navController.popBackStack()
                }
                is ViewState.Error -> {
                    binding.loginProgress.visibility = View.GONE
                    binding.loginPasswordInputLayout.isHelperTextEnabled = true
                    binding.loginEmailInputLayout.isHelperTextEnabled = true
                    val error = result.exception.message.toString()
                    if (error == "User not found") {
                        binding.loginEmailInputLayout.helperText = error
                    } else binding.loginPasswordInputLayout.helperText = error
                }
                else -> Log.d("LoginFragment", result.toString())
            }
        })
    }
}