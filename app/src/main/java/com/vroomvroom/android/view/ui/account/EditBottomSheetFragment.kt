package com.vroomvroom.android.view.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.databinding.FragmentEditBottomSheetBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.account.viewmodel.AccountViewModel
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel by viewModels<AccountViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private lateinit var binding: FragmentEditBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUser()

        binding.btnSave.setOnClickListener {
            val name = binding.nameInputEditText.text
            if (name.toString().filter { !it.isWhitespace() }.isNotBlank()) {
                viewModel.mutationUpdateName(name.toString())
            }
        }
    }

    private fun observeUser() {
        viewModel.user.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnCancel.isEnabled = false
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    authViewModel.updateUserName(response.result.updateName.id, response.result.updateName.name!!)
                    findNavController().popBackStack()
                }
                is ViewState.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.btnCancel.isEnabled = true
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}