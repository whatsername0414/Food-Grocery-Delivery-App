package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLocationBinding
import com.vroomvroom.android.view.ui.Utils.hasLocationPermission
import com.vroomvroom.android.view.ui.createLocationRequest
import com.vroomvroom.android.view.ui.requestLocationPermission
import com.vroomvroom.android.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LocationFragment : Fragment(R.layout.fragment_location), EasyPermissions.PermissionCallbacks {

    private val viewModel by viewModels<DataViewModel>()
    private var currentLocation: Location? = null
    private var isConnected: Boolean = false

    private lateinit var binding: FragmentLocationBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding = FragmentLocationBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.linearProgress.visibility = View.GONE
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    isConnected = true
                }
                override fun onLost(network: Network) {
                   isConnected = false
                }
            })
        }

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationRequest = LocationRequest.create().apply {
            priority = PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }

        binding.btnAllowLocation.setOnClickListener {
            if (isConnected) {
                if (hasLocationPermission(requireContext())) {
                    setLocation()
                } else {
                    createLocationRequest(requireActivity(), this )
                }
            } else {
                Snackbar.make(binding.root.rootView, R.string.snackbar_no_connection, Snackbar.LENGTH_SHORT)
                    .setDuration(2000)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                    .show()
            }
        }
        viewModel.location.observe(viewLifecycleOwner, { location ->
            if (location != null) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                findNavController().navigate(R.id.action_locationFragment_to_homeFragment)
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
            setLocation()
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

    @SuppressLint("MissingPermission")
    private fun setLocation() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
                if (currentLocation != null) {
                    viewModel.saveLocation("${currentLocation?.latitude}, ${currentLocation?.longitude}")
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
}