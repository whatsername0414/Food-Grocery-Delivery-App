package com.vroomvroom.android.view.ui.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentMapsBinding
import com.vroomvroom.android.utils.Utils.createLocationRequest
import com.vroomvroom.android.utils.Utils.geoCoder
import com.vroomvroom.android.utils.Utils.hasLocationPermission
import com.vroomvroom.android.utils.Utils.requestLocationPermission
import com.vroomvroom.android.utils.Utils.setSafeOnClickListener
import com.vroomvroom.android.utils.Utils.userLocationBuilder
import com.vroomvroom.android.view.ui.location.viewmodel.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MapsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private val viewModel by viewModels<LocationViewModel>()
    private var isConnected: Boolean = false
    private var newLatLng: LatLng? = null
    private var mapFragment: SupportMapFragment? = null
    private var autocompleteFragment: AutocompleteSupportFragment? = null
    private var map: GoogleMap? = null
    private var address: Address? = null

    private lateinit var binding: FragmentMapsBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater)
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        navController = findNavController()
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiKey = getString(R.string.google_maps_key)

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.mapsToolbar.setupWithNavController(navController, appBarConfiguration)
        val prevDestination = navController.previousBackStackEntry?.destination?.id

        observeCurrentLocation()

        autocompleteFragment?.setHint("Search your address")
        autocompleteFragment?.view
            ?.findViewById<AppCompatImageButton>(R.id.places_autocomplete_search_button)?.visibility = View.GONE
        autocompleteFragment?.view
            ?.findViewById<AppCompatImageButton>(R.id.places_autocomplete_clear_button)?.setPadding(0,0,32,0)
        autocompleteFragment
            ?.view?.findViewById<AppCompatEditText>(R.id.places_autocomplete_search_input)?.setPadding(32,0,0,0)
        autocompleteFragment?.setPlaceFields(listOf(Place.Field.LAT_LNG, Place.Field.NAME))
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }
        Places.createClient(requireContext())

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
            }
            override fun onLost(network: Network) {
                isConnected = false
            }
        })

        mapFragment?.getMapAsync { googleMap ->
            map = googleMap
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL

            map?.setOnCameraIdleListener {
                newLatLng = map?.cameraPosition?.target
                newLatLng?.let { latLng ->
                    address = geoCoder(requireContext(), latLng)
                    autocompleteFragment?.setText(address?.getAddressLine(0))
                    binding.cameraMoveSpinner.visibility = View.GONE
                }
            }

            map?.setOnCameraMoveListener {
                autocompleteFragment?.setText("")
                binding.cameraMoveSpinner.visibility = View.VISIBLE
            }

            viewModel.userLocation.observe(viewLifecycleOwner, { userLocation ->
                if (!userLocation.isNullOrEmpty()) {
                    val location = userLocation.find { it.current_use }
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(location!!.latitude, location.longitude), 17f))
                }
            })
        }

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i("MapsFragment", "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("MapsFragment", "An error occurred: $status")
            }
        })

        binding.cameraMoveSpinner.visibility = View.GONE
        binding.cvAutoComplete.visibility = View.VISIBLE
        binding.btnConfirmLocation.setOnClickListener {
            validateLocation(prevDestination)
        }

        binding.myCurrentLocation.setSafeOnClickListener {
            if (isConnected) {
                if (hasLocationPermission(requireContext())) {
                    viewModel.requestLocationUpdates()
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
    }

    private fun showDialog(prevDestination: Int?) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.maps_alert_dialog_title))
            .setMessage(getString(R.string.maps_alert_dialog_message))
            .setNeutralButton(getString(R.string.maps_alert_dialog_cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.maps_alert_dialog_proceed)) { _, _ ->
                insertLocation(prevDestination)
            }
            .show()
    }

    private fun insertLocation(prevDestination: Int?) {
        newLatLng?.let { latLng ->
            viewModel.insertLocation(
                userLocationBuilder(address = address, latLng = latLng)
            )
        }
        if (prevDestination == R.id.locationFragment) {
            navController.navigate(R.id.action_mapsFragment_to_homeFragment)
        } else {
            navController.popBackStack()
        }
    }

    private fun validateLocation(prevDestination: Int?) {
        if (newLatLng != null && newLatLng?.latitude != 0.0 && newLatLng?.longitude != 0.0 ) {
            if (address?.locality !in viewModel.deliveryRange) {
                showDialog(prevDestination)
            } else {
                if (address?.thoroughfare.isNullOrBlank() && newLatLng != null) {
                    navController.navigate(
                        MapsFragmentDirections.actionMapsFragmentToAddressBottomSheetFragment(
                            userLocationBuilder(address = address, latLng = newLatLng!!)
                        )
                    )
                } else {
                    insertLocation(prevDestination)
                }
            }
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.maps_alert_dialog_title))
                .setMessage(getString(R.string.maps_alert_dialog_message_invalid))
                .setNegativeButton(getString(R.string.maps_alert_dialog_understood)) { _, _ -> }
                .show()
        }
    }

    private fun observeCurrentLocation() {
        viewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                viewModel.removeLocationUpdates()
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude), 17f))
            }
        }
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
            requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        viewModel.requestLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mapFragment != null) {
            val ft = childFragmentManager.beginTransaction()
            ft.remove(mapFragment!!)
        }
    }
}