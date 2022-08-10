package com.vroomvroom.android.view.ui.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLocationBinding
import com.vroomvroom.android.utils.Utils.createLocationRequest
import com.vroomvroom.android.utils.Utils.hasLocationPermission
import com.vroomvroom.android.utils.Utils.requestLocationPermission
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.utils.Utils.userLocationBuilder
import com.vroomvroom.android.view.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LocationFragment : BaseFragment<FragmentLocationBinding>(
    FragmentLocationBinding::inflate
), EasyPermissions.PermissionCallbacks {

    private var isConnected: Boolean = false

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUserLocation()

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    isConnected = true
                }
                override fun onLost(network: Network) {
                   isConnected = false
                }
        })

        binding.btnAllowLocation.setOnClickListener {
            if (isConnected) {
                if (hasLocationPermission(requireContext())) {
                    binding.linearProgress.visibility = View.VISIBLE
                    locationViewModel.requestLocationUpdates()
                } else {
                    binding.linearProgress.visibility = View.GONE
                    createLocationRequest(requireActivity(), this )
                }
            } else {
                Snackbar.make(
                    binding.root.rootView,
                    R.string.snackbar_no_connection,
                    Snackbar.LENGTH_SHORT
                    )
                    .setDuration(2000)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                    .show()
            }
        }
        locationViewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                locationViewModel.removeLocationUpdates()
                val latLng = LatLng(location.latitude, location.longitude)
                locationViewModel.getAddress(latLng)
                locationViewModel.address.observe(viewLifecycleOwner) { res ->
                    if (res?.thoroughfare.isNullOrBlank()) {
                        findNavController().safeNavigate(
                            LocationFragmentDirections.
                            actionLocationFragmentToAddressBottomSheetFragment(
                                userLocationBuilder(
                                    address = res,
                                    latLng = latLng
                                )
                            )
                        )
                    } else {
                        locationViewModel.insertLocation(
                            userLocationBuilder(
                                address = res,
                                latLng = latLng
                            )
                        )
                    }
                }
            }
        }

        binding.bntEnterLocation.setOnClickListener {
            findNavController().safeNavigate(R.id.action_locationFragment_to_mapsFragment)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationViewModel.requestLocationUpdates()
        }
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            binding.linearProgress.visibility = View.GONE
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            binding.linearProgress.visibility = View.GONE
            requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
    }

    private fun observeUserLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { userLocation ->
            if (!userLocation.isNullOrEmpty()) {
                findNavController().popBackStack()
            }
        }
    }
}