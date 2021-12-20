package com.vroomvroom.android.view.ui.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLocationBinding
import com.vroomvroom.android.utils.Utils.createLocationRequest
import com.vroomvroom.android.utils.Utils.geoCoder
import com.vroomvroom.android.utils.Utils.hasLocationPermission
import com.vroomvroom.android.utils.Utils.requestLocationPermission
import com.vroomvroom.android.utils.Utils.userLocationBuilder
import com.vroomvroom.android.view.ui.location.viewmodel.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LocationFragment : Fragment(R.layout.fragment_location), EasyPermissions.PermissionCallbacks {

    private val viewModel by viewModels<LocationViewModel>()
    private var isConnected: Boolean = false

    private lateinit var binding: FragmentLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("MissingPermission", "ObsoleteSdkInt")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.linearProgress.visibility = View.GONE

        observeUserLocation()

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        isConnected = true
                    }
                    override fun onLost(network: Network) {
                       isConnected = false
                    }
            })
        }

        binding.btnAllowLocation.setOnClickListener {
            if (isConnected) {
                if (hasLocationPermission(requireContext())) {
                    viewModel.requestLocationUpdates()
                } else {
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
        viewModel.currentLocation.observe(viewLifecycleOwner, { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                val address = geoCoder(requireContext(), latLng)
                viewModel.insertLocation(userLocationBuilder(address = address, latLng = latLng))
            }
        })

        binding.bntEnterLocation.setOnClickListener {
            findNavController().navigate(R.id.action_locationFragment_to_mapsFragment)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            binding.linearProgress.visibility = View.VISIBLE
            viewModel.requestLocationUpdates()
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
        viewModel.userLocation.observe(viewLifecycleOwner, { userLocation ->
            if (!userLocation.isNullOrEmpty()) {
                viewModel.removeLocationUpdates()
                findNavController().popBackStack()
            }
        })
    }
}