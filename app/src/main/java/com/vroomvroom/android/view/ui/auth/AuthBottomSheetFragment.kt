package com.vroomvroom.android.view.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAuthBottomSheetBinding
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AuthBottomSheetFragment : BaseBottomSheetFragment<FragmentAuthBottomSheetBinding>(
    FragmentAuthBottomSheetBinding::inflate
) {

    private lateinit var getSignInWithGoogle : ActivityResultLauncher<Intent>
    private var currentLoginChoice = SignIntType.GOOGLE
    private var currentLocation: LocationEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView8.movementMethod = LinkMovementMethod.getInstance()
        binding.textView8.text = Html.fromHtml(getString(R.string.terms_and_policy), FROM_HTML_MODE_COMPACT)

        observeLocation()
        observeToken()
        observeRegisterUser()
        observeNewLoggedInUser()

        getSignInWithGoogle = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                authViewModel.googleSignIn(result.data)
            }
        }

        binding.btnEmailAddress.setOnClickListener {
            findNavController().navigate(R.id.action_authBottomSheetFragment_to_loginFragment)
        }

        binding.btnGoogle.setOnClickListener {
            getSignInWithGoogle.launch(authViewModel.signInIntent)
            currentLoginChoice = SignIntType.GOOGLE
        }

        binding.btnFacebook.setOnClickListener {
            authViewModel.facebookLogIn(this)
            currentLoginChoice = SignIntType.FACEBOOK
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        authViewModel.onActivityResult(requestCode, resultCode, data)
    }

    private fun observeLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { locations ->
            currentLocation = locations?.firstOrNull { it.currentUse }
        }
    }

    private fun observeToken() {
        authViewModel.token.observe(viewLifecycleOwner) { token ->
            if (token != null) {
                currentLocation?.let { authViewModel.register(it) }
            }
        }
    }

    private fun observeRegisterUser() {
        authViewModel.isRegistered.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
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
                                loadingDialog.show(getString(R.string.loading))
                                currentLocation?.let { authViewModel.register(it) }
                                dialog.dismiss()
                            }
                            ClickType.NEGATIVE -> Unit
                        }
                    }
                }
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
                    dialog.show(
                        getString(R.string.auth_failed),
                        getString(R.string.network_error_message),
                        getString(R.string.cancel),
                        getString(R.string.retry)
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                when (currentLoginChoice) {
                                    SignIntType.GOOGLE -> {
                                        getSignInWithGoogle.launch(authViewModel.signInIntent)
                                    }
                                    SignIntType.FACEBOOK -> {
                                        authViewModel.facebookLogIn(this)
                                    }
                                }
                            }
                            ClickType.NEGATIVE -> dialog.dismiss()
                        }
                    }
                }
            }
        }
    }
}