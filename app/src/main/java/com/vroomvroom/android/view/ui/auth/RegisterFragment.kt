package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentRegisterBinding
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.utils.Utils.isEmailValid
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class RegisterFragment : BaseFragment<FragmentRegisterBinding>(
    FragmentRegisterBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerProgress.visibility = View.GONE

        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()

        observeNewLogInUser()

        binding.txtLogin.setOnClickListener {
            navController.popBackStack()
        }
        binding.btnRegister.setOnClickListener {
            requireActivity().hideSoftKeyboard()
            val emailAddress = binding.registerEmailInputEditText.text.toString()
            val password = binding.registerPasswordInputEditText.text.toString()
            val confirmPassword = binding.registerConfirmPasswordInputEditText.text.toString()
            if (emailAddress.isEmailValid()) {
                if (password == confirmPassword) {
                    binding.registerProgress.visibility = View.VISIBLE
                    binding.errorTv.visibility = View.GONE
                    authViewModel.registerWithEmailAndPassword(emailAddress, password)
                } else {
                    binding.errorTv.visibility = View.VISIBLE
                    binding.errorTv.text = getString(R.string.invalid_password)
                }
            } else {
                binding.errorTv.visibility = View.VISIBLE
                binding.errorTv.text = getString(R.string.invalid_email_address)
            }
        }
    }

    private fun observeNewLogInUser() {
        authViewModel.newLoggedInUser.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ViewState.Success -> {
                    binding.errorTv.visibility = View.GONE
                    requireActivity().hideSoftKeyboard()
                    navController.navigate(R.id.action_registerFragment_to_checkoutFragment)
                }
                is ViewState.Error -> {
                    binding.registerProgress.visibility = View.GONE
                    binding.errorTv.visibility = View.VISIBLE
                    binding.errorTv.text = result.exception.message
                }
                else -> Unit
            }
        }
    }
}