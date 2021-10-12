package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
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
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
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
import com.google.android.material.textfield.TextInputEditText
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentMapsBinding
import com.vroomvroom.android.view.ui.Utils.hasLocationPermission
import com.vroomvroom.android.view.ui.Utils.hideSoftKeyboard
import com.vroomvroom.android.view.ui.Utils.createLocationRequest
import com.vroomvroom.android.view.ui.Utils.requestLocationPermission
import com.vroomvroom.android.view.ui.Utils.setSafeOnClickListener
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.IOException

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MapsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private val viewModel by viewModels<MainViewModel>()
    private var isConnected: Boolean = false
    private var newLatLng: LatLng? = null
    private var mapFragment: SupportMapFragment? = null
    private var autocompleteFragment: AutocompleteSupportFragment? = null
    private var toStringCoordinate: String? =null
    private var map: GoogleMap? = null
    private var currentCity: String? = null
    private val deliveryRange: List<String> = arrayListOf("Bacacay", "Legazpi City",
        "Ligao", "Malilipot", "Malinao", "Santo Domingo", "Tabaco City", "Tiwi")

    private lateinit var binding: FragmentMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupIU(binding.root)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiKey = getString(R.string.google_maps_key)

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

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.mapsToolbar.setupWithNavController(navController, appBarConfiguration)

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }

        mapFragment?.getMapAsync { googleMap ->
            map = googleMap
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL

            map?.setOnCameraIdleListener {
                newLatLng = map?.cameraPosition?.target
                val geoCoder = Geocoder(requireContext())
                if (newLatLng != null) {
                    try {
                        val currentLocation = geoCoder.getFromLocation(
                            newLatLng!!.latitude,
                            newLatLng!!.longitude,
                            1
                        )
                        toStringCoordinate = "${newLatLng!!.latitude}, ${newLatLng!!.longitude}"
                        if (currentLocation.isNotEmpty()) {
                            val location = currentLocation.first()
                            currentCity = location.locality
                            autocompleteFragment?.setText(location.getAddressLine(0))
                            binding.cameraMoveSpinner.visibility = View.GONE
                        }
                    } catch (e: IOException) {
                        Toast.makeText(requireContext(), "Unstable Network", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            }

            map?.setOnCameraMoveListener {
                autocompleteFragment?.setText("")
                binding.cameraMoveSpinner.visibility = View.VISIBLE
            }

            viewModel.location.observe(viewLifecycleOwner, { location ->
                if (location != null && mapFragment != null) {
                    val coordinates = location.split(", ")
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(coordinates[0].toDouble(), coordinates[1].toDouble()), 17f))
                }
            })

            viewModel.currentLocation.observe(viewLifecycleOwner) { location ->
                if (location != null) {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude), 17f))
                }
            }
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
            if (toStringCoordinate != null && toStringCoordinate != "0.0, 0.0") {
                if (currentCity !in deliveryRange) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(resources.getString(R.string.maps_alert_dialog_title))
                        .setMessage(resources.getString(R.string.maps_alert_dialog_message))
                        .setNeutralButton(resources.getString(R.string.maps_alert_dialog_cancel)) { _, _ ->
                            // Ignore
                        }
                        .setPositiveButton(resources.getString(R.string.maps_alert_dialog_proceed)) { _, _ ->
                            viewModel.saveLocation(toStringCoordinate!!)
                            navController.popBackStack()
                        }
                        .show()
                } else {
                    viewModel.saveLocation(toStringCoordinate!!)
                    navController.popBackStack()
                }
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.maps_alert_dialog_title))
                    .setMessage(resources.getString(R.string.maps_alert_dialog_message_invalid))
                    .setNegativeButton(resources.getString(R.string.maps_alert_dialog_understood)) { _, _ ->
                        // Ignore
                    }
                    .show()
            }
        }

        binding.myCurrentLocation.setSafeOnClickListener {
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

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupIU(view: View) {
        if (view !is TextInputEditText) {
            view.setOnTouchListener { _, _ ->
                requireActivity().hideSoftKeyboard()
                false
            }
        }
        if (view is TextInputEditText) {
            view.setOnTouchListener { _, _ ->
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupIU(innerView)
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
        setLocation()
    }

    @SuppressLint("MissingPermission")
    private fun setLocation() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    viewModel.currentLocation.postValue(location)
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mapFragment != null) {
            val ft = childFragmentManager.beginTransaction()
            ft.remove(mapFragment!!)
        }
    }
}