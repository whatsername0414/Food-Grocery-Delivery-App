package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLoginBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Utils.hideSoftKeyboard
import com.vroomvroom.android.utils.Utils.isEmailValid
import com.vroomvroom.android.view.resource.Resource
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
        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()

        observeNewLoggedInUser()
        observeToken()
        observeGetUser()

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
                authViewModel.logInWithEmailAndPassword(emailAddress, password)
            } else {
                binding.errorTv.visibility = View.VISIBLE
                binding.errorTv.text = getString(R.string.invalid_email_address)
            }
        }
    }

    private fun observeGetUser() {
        accountViewModel.isGetUserSuccessful.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    navController.popBackStack()
                }
                is Resource.Error -> {
                    accountViewModel.getUser()
                    loadingDialog.dismiss()
                    dialog.show(
                        getString(R.string.network_error),
                        getString(R.string.unsaved_error),
                        getString(R.string.cancel),
                        getString(R.string.retry),
                        isButtonLeftVisible = false,
                        isCancellable = false
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                accountViewModel.getUser()
                                dialog.dismiss()
                            }
                            ClickType.NEGATIVE -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun observeToken() {
        authViewModel.token.observe(viewLifecycleOwner) { token ->
            if (token != null) {
                accountViewModel.getUser()
            }
        }
    }

    private fun observeNewLoggedInUser() {
        authViewModel.newLoggedInUser.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    loadingDialog.show(getString(R.string.loading))
                }
                is Resource.Success -> {
                    authViewModel.saveIdToken()
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    dialog.show(
                        getString(R.string.login_failed),
                        result.exception.message.orEmpty(),
                        getString(R.string.ok),
                        getString(R.string.ok),
                        isButtonLeftVisible = false,
                        isCancellable = false
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                dialog.dismiss()
                            }
                            ClickType.NEGATIVE -> Unit
                        }
                    }
                }
            }
        }
    }
}