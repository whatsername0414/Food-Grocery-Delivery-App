package com.vroomvroom.android.view.ui.main.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAuthBottomSheetBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.AuthViewModel
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

        binding.authProgress.visibility = View.GONE

        observeNewLoggedInUser()

        val getSignInWithGoogle = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.taskGoogleSignIn(result.data)
            }
        }

        binding.btnEmail.setOnClickListener {
            findNavController().navigate(R.id.action_authBottomSheetFragment_to_loginFragment)
        }

        binding.btnGoogle.setOnClickListener {
            binding.authProgress.visibility = View.VISIBLE
            getSignInWithGoogle.launch(viewModel.signInIntent)
        }

        binding.btnFacebook.setOnClickListener {
            binding.authProgress.visibility = View.VISIBLE
            viewModel.facebookLogIn(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    private fun observeNewLoggedInUser() {
        viewModel.newLoggedInUser.observe(viewLifecycleOwner, { result ->
            when (result) {
                is ViewState.Success -> findNavController().popBackStack()
                is ViewState.Error -> {
                    binding.authProgress.visibility = View.GONE
                    val error = result.exception.message
                    Toast.makeText(
                        requireContext(),
                        error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Log.d("AuthBottomSheetFragment", result.toString())
            }
        })
    }
}