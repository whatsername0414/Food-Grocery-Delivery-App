package com.vroomvroom.android.view.ui.location

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAddressesBinding
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.location.adapter.AddressAdapter
import com.vroomvroom.android.view.ui.widget.CommonAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddressesFragment : BaseFragment<FragmentAddressesBinding>(
    FragmentAddressesBinding::inflate
) {
    private val args: AddressesFragmentArgs by navArgs()
    private val adapter by lazy { AddressAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()

        binding.addressRv.adapter = adapter
        observeUserLocation()

        adapter.onAddressClicked = { address ->
            locationViewModel.clickedAddress = address
            locationViewModel.updateLocation(address.use())
        }

        adapter.onDeleteClicked = { address ->
            val currentAddressList = adapter.currentList
            val lastNotUseAddress = currentAddressList.last { !it.currentUse }
            if (address.currentUse) {
                locationViewModel.updateLocation(lastNotUseAddress.use())
            }
            if (currentAddressList.size > 1) {
                locationViewModel.deleteLocation(address)
            }
        }

        binding.btnAddAddress.setOnClickListener {
            navController.navigate(R.id.action_addressesFragment_to_mapsFragment)
        }

        if (args.orderId != null) {
            observeChangeAddress()
            binding.btnSave.visibility = View.VISIBLE
            adapter.currentUseAddress =  { address ->
                    binding.btnSave.setOnClickListener{
                        ordersViewModel
                            .mutationUpdateDeliveryAddress(args.orderId ?: "", address)
                }
            }
        }
    }

    private fun observeUserLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { locations ->
            adapter.submitList(locations)
        }
    }

    private fun observeChangeAddress() {
        ordersViewModel.changeAddress.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        response.result.updateDeliveryAddress,
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                }
                is ViewState.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    initAlertDialog()
                }
            }
        }
    }

    private fun initAlertDialog() {
        val dialog = CommonAlertDialog(
            requireActivity()
        )
        dialog.show(
            getString(R.string.network_error),
            getString(R.string.network_error_message),
            getString(R.string.cancel),
            getString(R.string.retry)
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    adapter.currentUseAddress = { address ->
                        ordersViewModel.mutationUpdateDeliveryAddress(args.orderId ?: "", address)
                    }
                    dialog.dismiss()
                }
                ClickType.NEGATIVE -> dialog.dismiss()
            }
        }
    }

    private fun UserLocationEntity.use(): UserLocationEntity {
        return UserLocationEntity(
            this.id,
            this.address,
            this.city,
            this.addInfo,
            this.latitude,
            this.longitude,
            true
        )
    }
}