package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentPasswordResetBottomSheetBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Utils.isEmailValid
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PasswordResetBottomSheetFragment : BaseBottomSheetFragment<FragmentPasswordResetBottomSheetBinding>(
    FragmentPasswordResetBottomSheetBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.passwordResetProgress.visibility = View.GONE

        observeIsResetPasswordEmailSent()

        binding.btnReset.setOnClickListener {
            sendEmail()
        }

    }

    private fun observeIsResetPasswordEmailSent() {
        authViewModel.isPasswordResetEmailSent.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(), "Email sent",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    dialog.show(
                        getString(R.string.network_error),
                        getString(R.string.network_error_message),
                        getString(R.string.cancel),
                        getString(R.string.retry),
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                sendEmail()
                                dialog.dismiss()
                            }
                            ClickType.NEGATIVE -> Unit
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    private fun sendEmail() {
        val emailAddress = binding.resetEmailInputEditText.text.toString()
        if (emailAddress.isEmailValid()) {
            binding.passwordResetProgress.visibility = View.VISIBLE
            authViewModel.resetPasswordWithEmail(emailAddress)
        } else binding.resetEmailInputLayout.helperText = "Invalid email"
    }
}