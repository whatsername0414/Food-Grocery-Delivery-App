package com.vroomvroom.android.view.ui.account

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAccountBinding
import com.vroomvroom.android.data.model.account.AccountMenuOptionItem
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.view.ui.account.adapter.OptionAdapter
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.common.CommonAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AccountFragment : BaseFragment<FragmentAccountBinding>(
    FragmentAccountBinding::inflate
) {

    private val options = arrayListOf<AccountMenuOptionItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUser()
    }

    private fun observeUser() {
        mainActivityViewModel.user.observe(viewLifecycleOwner) { user ->
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

    private fun initAlertDialog(
        title: String,
        message: String,
        leftButtonTitle: String,
        rightButtonTitle: String
    ) {
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
                AccountMenuOptionType.PROFILE ->
                    findNavController().navigate(R.id.action_accountFragment_to_profileManagementFragment)
                AccountMenuOptionType.ADDRESSES ->
                    findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToAddressesFragment(null))
                AccountMenuOptionType.FAVORITES ->
                    findNavController().navigate(R.id.action_accountFragment_to_favoriteFragment)
                AccountMenuOptionType.ABOUT ->
                    findNavController().navigate(R.id.action_accountFragment_to_aboutFragment)
                AccountMenuOptionType.LOGIN ->
                    findNavController().navigate(R.id.action_accountFragment_to_authBottomSheetFragment)
                AccountMenuOptionType.LOGOUT ->
                    initAlertDialog(
                        getString(R.string.logout),
                        getString(R.string.logout_confirmation_message),
                        getString(R.string.no),
                        getString(R.string.yes)
                    )
            }
        }
    }

    private fun getOptionItems(hasUser: Boolean): List<AccountMenuOptionItem> {
        val profile = AccountMenuOptionItem(
            R.drawable.ic_profile,
            R.string.profile,
            AccountMenuOptionType.PROFILE
        )
        val addresses = AccountMenuOptionItem(
            R.drawable.ic_location_outline,
            R.string.addresses,
            AccountMenuOptionType.ADDRESSES
        )
        val favorites = AccountMenuOptionItem(
            R.drawable.ic_love,
            R.string.favorites,
            AccountMenuOptionType.FAVORITES
        )
        val about = AccountMenuOptionItem(
            R.drawable.ic_info,
            R.string.about,
            AccountMenuOptionType.ABOUT
        )
        val login = AccountMenuOptionItem(
            R.drawable.ic_login,
            R.string.login_or_sign_up,
            AccountMenuOptionType.LOGIN
        )
        val logout = AccountMenuOptionItem(
            R.drawable.ic_logout,
            R.string.logout,
            AccountMenuOptionType.LOGOUT
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