package com.vroomvroom.android.view.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAccountBinding
import com.vroomvroom.android.databinding.FragmentProfileBinding
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AccountFragment : Fragment() {

    private val authViewModel by viewModels<AuthViewModel>()
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUser()

        binding.profileConstraint.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_profileManagementFragment)
        }

        binding.addressesConstraint.setOnClickListener {
            findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToAddressesFragment(null))
        }

        binding.favoriteConstraint.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_favoriteFragment)
        }

    }

    private fun observeUser() {
        authViewModel.userRecord.observe(viewLifecycleOwner, { users ->
            if (!users.isNullOrEmpty()) {
                binding.profileConstraint.visibility = View.VISIBLE
                binding.addressesConstraint.visibility = View.VISIBLE
                binding.favoriteConstraint.visibility = View.VISIBLE
                binding.initial.text = users.first().name?.get(0).toString()
                binding.name.text = users.first().name
                binding.initialBg.visibility = View.VISIBLE
                binding.initial.visibility = View.VISIBLE
                binding.name.visibility = View.VISIBLE
            } else {
                binding.profileConstraint.visibility = View.GONE
                binding.addressesConstraint.visibility = View.GONE
                binding.favoriteConstraint.visibility = View.GONE
                binding.initialBg.visibility = View.GONE
                binding.initial.visibility = View.GONE
                binding.name.visibility = View.GONE
                findNavController().navigate(R.id.action_accountFragment_to_authBottomSheetFragment)
            }
        })
    }
}