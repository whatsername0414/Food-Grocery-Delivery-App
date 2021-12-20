package com.vroomvroom.android.view.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAddressesBinding
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.view.ui.location.adapter.AddressAdapter
import com.vroomvroom.android.view.ui.location.viewmodel.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddressesFragment : Fragment() {

    private val viewModel by viewModels<LocationViewModel>()
    private val adapter by lazy { AddressAdapter() }

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
            viewModel.updateLocation(address.use())
        }

        adapter.onDeleteClicked = { address ->
            viewModel.deleteLocation(address.use())
            val lastUserLocation = viewModel.userLocation.value?.last()
            if (address.current_use) {
                lastUserLocation?.let {
                    viewModel.updateLocation(it.use())
                }
            }
        }

        binding.addAddress.setOnClickListener {
            navController.navigate(R.id.action_addressesFragment_to_mapsFragment)
        }

    }

    private fun observeUserLocation() {
        viewModel.userLocation.observe(viewLifecycleOwner, { locations ->
            adapter.submitList(locations)
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