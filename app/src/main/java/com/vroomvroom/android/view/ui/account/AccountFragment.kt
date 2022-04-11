package com.vroomvroom.android.view.ui.account

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAccountBinding
import com.vroomvroom.android.domain.model.account.AccountOptionItem
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.view.ui.account.adapter.OptionAdapter
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.widget.CommonAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AccountFragment : BaseFragment<FragmentAccountBinding>(
    FragmentAccountBinding::inflate
) {

    private val options = arrayListOf<AccountOptionItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUser()

        authViewModel.token.observe(viewLifecycleOwner) { token ->
            Log.d("AccountFragment", token.toString())
        }
    }

    private fun observeUser() {
        authViewModel.userRecord.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                initOptionAdapter(true)
                binding.accountIcon.visibility = View.GONE
                binding.initialBg.visibility = View.VISIBLE
                binding.initial.visibility = View.VISIBLE
                binding.initial.text = user.name?.get(0).toString()
                binding.name.text = user.name
            } else {
                initOptionAdapter(false)
                binding.initialBg.visibility = View.GONE
                binding.initial.visibility = View.GONE
                binding.accountIcon.visibility = View.VISIBLE
                binding.name.text = getString(R.string.app_name)
            }
        }
    }

    private fun performLogout() {
        authViewModel.logoutUser { successful ->
            if (successful) {
                authViewModel.deleteUserRecord()
                authViewModel.clearDataStore()
                locationViewModel.deleteAllAddress()

            } else {
                initAlertDialog(
                    getString(R.string.network_error),
                    getString(R.string.network_error_message),
                    getString(R.string.cancel),
                    getString(R.string.retry)
                )
            }
        }
    }

    private fun initAlertDialog(
        title: String,
        message: String,
        leftButtonTitle: String,
        rightButtonTitle: String
    ) {
        val dialog = CommonAlertDialog(requireActivity())
        dialog.show(
            title,
            message,
            leftButtonTitle,
            rightButtonTitle
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    performLogout()
                    dialog.dismiss()
                }
                ClickType.NEGATIVE -> dialog.dismiss()
            }
        }
    }

    private fun initOptionAdapter(hasUser: Boolean) {
        options.clear()
        binding.optionRv.adapter = OptionAdapter(getOptionItems(hasUser)) { type ->
            when (type) {
                AccountOptionType.PROFILE ->
                    findNavController().navigate(R.id.action_accountFragment_to_profileManagementFragment)
                AccountOptionType.ADDRESSES ->
                    findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToAddressesFragment(null))
                AccountOptionType.FAVORITES ->
                    findNavController().navigate(R.id.action_accountFragment_to_favoriteFragment)
                AccountOptionType.ABOUT ->
                    findNavController().navigate(R.id.action_accountFragment_to_aboutFragment)
                AccountOptionType.LOGIN ->
                    findNavController().navigate(R.id.action_accountFragment_to_authBottomSheetFragment)
                AccountOptionType.LOGOUT ->
                    initAlertDialog(
                        getString(R.string.prompt),
                        getString(R.string.logout_confirmation_message),
                        getString(R.string.no),
                        getString(R.string.yes)
                    )
            }
        }
    }

    private fun getOptionItems(hasUser: Boolean): List<AccountOptionItem> {
        val profile = AccountOptionItem(
            R.drawable.ic_profile,
            R.string.profile,
            AccountOptionType.PROFILE
        )
        val addresses = AccountOptionItem(
            R.drawable.ic_location_outline,
            R.string.addresses,
            AccountOptionType.ADDRESSES
        )
        val favorites = AccountOptionItem(
            R.drawable.ic_love,
            R.string.favorites,
            AccountOptionType.FAVORITES
        )
        val about = AccountOptionItem(
            R.drawable.ic_info,
            R.string.about,
            AccountOptionType.ABOUT
        )
        val login = AccountOptionItem(
            R.drawable.ic_login,
            R.string.login_or_sign_up,
            AccountOptionType.LOGIN
        )
        val logout = AccountOptionItem(
            R.drawable.ic_logout,
            R.string.logout,
            AccountOptionType.LOGOUT
        )

        if (hasUser) {
            options.add(profile)
            options.add(addresses)
            options.add(favorites)
            options.add(about)
            options.add(logout)
        } else {
            options.add(addresses)
            options.add(about)
            options.add(login)
        }

        return options
    }
}