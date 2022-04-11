package com.vroomvroom.android.view.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.vroomvroom.android.databinding.FragmentPasswordResetBottomSheetBinding
import com.vroomvroom.android.utils.Utils.isEmailValid
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PasswordResetBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel by activityViewModels<AuthViewModel>()

    private lateinit var binding: FragmentPasswordResetBottomSheetBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPasswordResetBottomSheetBinding.inflate(inflater)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.passwordResetProgress.visibility = View.GONE

        observeIsResetPasswordEmailSent()

        binding.btnReset.setOnClickListener {
            val emailAddress = binding.resetEmailInputEditText.text.toString()
            if (emailAddress.isEmailValid()) {
                binding.passwordResetProgress.visibility = View.VISIBLE
                viewModel.resetPasswordWithEmail(emailAddress)
            } else binding.resetEmailInputLayout.helperText = "Invalid email"
        }

    }

    private fun observeIsResetPasswordEmailSent() {
        viewModel.isPasswordResetEmailSent.observe(viewLifecycleOwner, { result ->
            when (result) {
                is ViewState.Success -> {
                    Toast.makeText(requireContext(), "Email sent",
                        Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is ViewState.Error -> {
                    Toast.makeText(requireContext(), result.exception.message,
                        Toast.LENGTH_SHORT).show()
                }
                else -> Log.d("PasswordResetBottomSheetFragment", result.toString())
            }
        })
    }
}