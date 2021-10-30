package com.vroomvroom.android.view.ui.main.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentRegisterBinding
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.utils.Utils.isEmailValid
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class RegisterFragment : Fragment() {

    private val viewModel by viewModels<AuthViewModel>()

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)

        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerProgress.visibility = View.GONE

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.registerToolbar.setupWithNavController(navController, appBarConfiguration)

        observeNewLogInUser()

        binding.txtLogin.setOnClickListener {
            navController.popBackStack()
        }
        binding.btnRegister.setOnClickListener {
            binding.registerEmailInputLayout.isHelperTextEnabled = false
            binding.registerConfirmPasswordInputLayout.isHelperTextEnabled = false
            val emailAddress = binding.registerEmailInputEditText.text.toString()
            val password = binding.registerPasswordInputEditText.text.toString()
            val confirmPassword = binding.registerConfirmPasswordInputEditText.text.toString()
            if (emailAddress.isEmailValid()) {
                if (password == confirmPassword) {
                    binding.registerProgress.visibility = View.VISIBLE
                    viewModel.registerWithEmailAndPassword(emailAddress, password)
                } else {
                    binding.registerConfirmPasswordInputLayout.isHelperTextEnabled = true
                    binding.registerConfirmPasswordInputLayout.helperText = "Passwords don't match"
                }
            } else {
                binding.registerEmailInputLayout.isHelperTextEnabled = true
                binding.registerEmailInputLayout.helperText = "Invalid email"
            }
        }
    }

    private fun observeNewLogInUser() {
        viewModel.newLoggedInUser.observe(viewLifecycleOwner, { result ->
            when (result) {
                is ViewState.Success -> {
                    requireActivity().hideSoftKeyboard()
                    navController.navigate(R.id.action_registerFragment_to_checkoutFragment)
                }
                is ViewState.Error -> {
                    binding.registerProgress.visibility = View.GONE
                    Log.w(
                        "RegisterFragment",
                        "registerWithEmailAndPassword:failure",
                        result.exception
                    )
                    binding.registerEmailInputLayout.isHelperTextEnabled = true
                    binding.registerEmailInputLayout.helperText = result.exception.message
                    Toast.makeText(
                        requireContext(),
                        result.exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Log.w("RegisterFragment", result.toString())
            }
        })
    }
}