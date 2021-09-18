package com.vroomvroom.android.view.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLocationBinding
import com.vroomvroom.android.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LocationFragment : Fragment(R.layout.fragment_location), EasyPermissions.PermissionCallbacks {

    private val viewModel by viewModels<DataViewModel>()
    private lateinit var binding: FragmentLocationBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var newLocation: String

    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding = FragmentLocationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.allowLocation.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(this, "Need location permission",
            PERMISSION_LOCATION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        setLocation()
    }

    private fun setLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geoCoder = Geocoder(requireContext())
                    val currentLocation = geoCoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )
                    newLocation = currentLocation.first().getAddressLine(0).toString()
                    viewModel.saveLocation(newLocation)
                    findNavController().navigate(R.id.merchantFragment)
                } else {
                    Log.e("LocationFragment", "Please Retry")
                }
            }
        }
    }
}