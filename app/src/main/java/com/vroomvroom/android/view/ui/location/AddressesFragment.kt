package com.vroomvroom.android.view.ui.location

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAddressesBinding
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.location.adapter.AddressAdapter
import com.vroomvroom.android.view.ui.location.viewmodel.LocationViewModel
import com.vroomvroom.android.view.ui.orders.viewmodel.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddressesFragment : Fragment() {

    private val viewModel by viewModels<LocationViewModel>()
    private val ordersViewModel by viewModels<OrdersViewModel>()
    private val adapter by lazy { AddressAdapter() }
    private val args: AddressesFragmentArgs by navArgs()

    private lateinit var binding: FragmentAddressesBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddressesBinding.inflate(inflater)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.addressRv.adapter = adapter
        observeUserLocation()

        adapter.onAddressClicked = { address ->
            viewModel.clickedAddress = address
            viewModel.updateLocation(address.use())
        }

        adapter.onDeleteClicked = { address ->
            viewModel.userLocation.observe(viewLifecycleOwner, { locations ->
                if (address.current_use && !locations.isNullOrEmpty()) {
                    viewModel.updateLocation(locations.last().use())
                }
                if (locations.size > 1) {
                    viewModel.deleteLocation(address)
                }
            })
        }

        binding.btnAddAddress.setOnClickListener {
            navController.navigate(R.id.action_addressesFragment_to_mapsFragment)
        }

        if (args.orderId != null) {
            observeChangeAddress()
            binding.btnSave.visibility = View.VISIBLE
            binding.btnSave.setOnClickListener {
                viewModel.clickedAddress?.let { address ->
                    ordersViewModel.mutationUpdateDeliveryAddress(args.orderId!!, address)
                }
            }
        }
    }

    private fun observeUserLocation() {
        viewModel.userLocation.observe(viewLifecycleOwner, { locations ->
            adapter.submitList(locations)
        })
    }

    private fun observeChangeAddress() {
        ordersViewModel.changeAddress.observe(viewLifecycleOwner, { response ->
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
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
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