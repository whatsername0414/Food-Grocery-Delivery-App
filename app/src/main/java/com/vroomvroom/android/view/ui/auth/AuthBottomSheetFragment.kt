package com.vroomvroom.android.view.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAuthBottomSheetBinding
import com.vroomvroom.android.domain.db.user.UserEntity
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AuthBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel by activityViewModels<AuthViewModel>()
    private lateinit var binding: FragmentAuthBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBottomSheetBinding.inflate(inflater)
        binding.textView8.movementMethod = LinkMovementMethod.getInstance()
        binding.textView8.text = Html.fromHtml(getString(R.string.terms_and_policy), FROM_HTML_MODE_COMPACT)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeToken()
        observeRegisterUser()
        observeNewLoggedInUser()

        val getSignInWithGoogle = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.taskGoogleSignIn(result.data)
            }
        }

        binding.btnEmail.setOnClickListener {
            findNavController().navigate(R.id.action_authBottomSheetFragment_to_loginFragment)
        }

        binding.btnGoogle.setOnClickListener {
            binding.progressIndicator.visibility = View.VISIBLE
            getSignInWithGoogle.launch(viewModel.signInIntent)
        }

        binding.btnFacebook.setOnClickListener {
            binding.progressIndicator.visibility = View.VISIBLE
            viewModel.facebookLogIn(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    private fun observeToken() {
        viewModel.token.observe(viewLifecycleOwner, { token ->
            if (token != null) {
                viewModel.register()
            }
        })
    }

    private fun observeRegisterUser() {
        viewModel.user.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    val result = response.result.register
                    result?.let {
                        val user = UserEntity(it.id, it.name, it.email, it.phone_number)
                        viewModel.insertUserRecord(user)
                    }
                    findNavController().popBackStack()
                }
                is ViewState.Error -> {
                    Snackbar.make(
                        binding.root,
                        R.string.snackbar_label,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.retry) {
                            viewModel.register()
                        }.show()
                }
            }
        })
    }

    private fun observeNewLoggedInUser() {
        viewModel.newLoggedInUser.observe(viewLifecycleOwner, { result ->
            when (result) {
                is ViewState.Loading -> {
                }
                is ViewState.Success -> {
                    viewModel.saveIdToken()
                }
                is ViewState.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    val error = result.exception.message
                    Toast.makeText(
                        requireContext(),
                        error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}