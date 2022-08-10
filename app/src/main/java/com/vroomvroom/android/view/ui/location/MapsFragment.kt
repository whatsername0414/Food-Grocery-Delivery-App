package com.vroomvroom.android.view.ui.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.BoundingBox
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentMapsBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Constants.DELIVERY_RANGE_CITIES
import com.vroomvroom.android.utils.Utils.createLocationRequest
import com.vroomvroom.android.utils.Utils.hasLocationPermission
import com.vroomvroom.android.utils.Utils.requestLocationPermission
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.utils.Utils.setSafeOnClickListener
import com.vroomvroom.android.utils.Utils.userLocationBuilder
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.location.adapter.SuggestionAdapter
import com.vroomvroom.android.view.ui.common.CommonAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MapsFragment : BaseFragment<FragmentMapsBinding>(
    FragmentMapsBinding::inflate
), EasyPermissions.PermissionCallbacks {

    private var isConnected: Boolean = false
    private var newLatLng: LatLng? = null
    private var mapFragment: SupportMapFragment? = null
    private var map: GoogleMap? = null
    private var address: Address? = null
    private var mSuggestions = listOf<SearchSuggestion>()

    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: SearchRequestTask

    private val suggestionAdapter by lazy { SuggestionAdapter() }
    private val searchCallback = object : SearchSelectionCallback {
        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            map?.clear()
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(
                result.coordinate?.latitude() ?: 0.0,
                result.coordinate?.longitude() ?: 0.0), 17f))
            binding.searchView.clearFocus()
        }
        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo) {
            mSuggestions = suggestions
            suggestionAdapter.submitList(suggestions)
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {}

        override fun onError(e: Exception) {
            showShortToast(R.string.general_error_message)
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationViewModel.initMapBoxDirectionClient(getString(R.string.mapbox_access_token))
        initSearchView()
        observeAddress()
        searchEngine = MapboxSearchSdk.getSearchEngine()

        suggestionAdapter.onSuggestionClicked = { suggestion ->
            searchRequestTask = searchEngine.select(suggestion, searchCallback)
        }

        binding.searchSuggestionRv.adapter = suggestionAdapter
        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.btnContinue.visibility = View.GONE
                binding.searchSuggestionRv.visibility = View.VISIBLE
                binding.searchView.queryHint = getString(R.string.search_location)
            } else {
                binding.searchSuggestionRv.visibility = View.GONE
                binding.btnContinue.visibility = View.VISIBLE
            }
        }

        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        navController = findNavController()
        binding.mapsToolbar.setupToolbar()
        val prevDestination = navController.previousBackStackEntry?.destination?.id

        observeCurrentLocation()
        onBackPressed()
        loadingDialog.show(getString(R.string.init_map))

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
            }
            override fun onLost(network: Network) {
                isConnected = false
            }
        })

        binding.btnContinue.setOnClickListener {
            validateLocation(prevDestination)
        }

        binding.myCurrentLocation.setSafeOnClickListener {
            if (isConnected) {
                if (hasLocationPermission(requireContext())) {
                    locationViewModel.requestLocationUpdates()
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

    private fun initSearchView() {
        val bBox = BoundingBox.fromLngLats(123.4539,13.0246,123.9611,13.498)
        binding.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if ((query?.length ?: 0) < 3) {
                        showShortToast(R.string.search_minimum)
                        return false
                    }
                    searchRequestTask = searchEngine.search(
                        query.orEmpty(),
                        SearchOptions(boundingBox = bBox),
                        searchCallback
                    )
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchRequestTask = searchEngine.search(
                        newText.orEmpty(),
                        SearchOptions(boundingBox = bBox),
                        searchCallback
                    )
                    return false
                }
            })
        }
    }

    private fun saveAddress(prevDestination: Int?) {
        newLatLng?.let { latLng ->
            locationViewModel.insertLocation(
                userLocationBuilder(address = address, latLng = latLng)
            )
        }
        if (prevDestination == R.id.locationFragment) {
            navController.safeNavigate(R.id.action_mapsFragment_to_homeFragment)
        } else {
            navController.popBackStack()
        }
    }

    private fun validateLocation(prevDestination: Int?) {
        val dialog = CommonAlertDialog(requireActivity())
        if (newLatLng != null && newLatLng?.latitude != 0.0 && newLatLng?.longitude != 0.0 ) {
            if (address?.locality !in DELIVERY_RANGE_CITIES) {
                dialog.show(
                    getString(R.string.prompt),
                    getString(R.string.maps_range_error_message),
                    getString(R.string.cancel),
                    getString(R.string.proceed)
                ) { type ->
                    when (type) {
                        ClickType.POSITIVE -> {
                            saveAddress(prevDestination)
                            dialog.dismiss()
                        }
                        ClickType.NEGATIVE -> dialog.dismiss()
                    }
                }
            } else {
                if (address?.thoroughfare.isNullOrBlank() && newLatLng != null) {
                    mainActivityViewModel.prevDestination = prevDestination
                    navController.navigate(
                        MapsFragmentDirections.actionMapsFragmentToAddressBottomSheetFragment(
                            userLocationBuilder(address = address, latLng = newLatLng!!)
                        )
                    )
                } else {
                    saveAddress(prevDestination)
                }
            }
        } else {
            dialog.show(
                getString(R.string.prompt),
                getString(R.string.maps_invalid_address),
                getString(R.string.cancel),
                getString(R.string.okay),
                false
            ) { type ->
                when (type) {
                    ClickType.POSITIVE -> dialog.dismiss()
                    ClickType.NEGATIVE -> Unit
                }
            }
        }
    }

    private fun getMap() {
        mapFragment?.getMapAsync { googleMap ->
            map = googleMap
            observeUserLocation()
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL

            map?.setOnCameraIdleListener {
                newLatLng = map?.cameraPosition?.target
                newLatLng?.let { latLng ->
                    locationViewModel.getAddress(latLng)
                }
            }
            loadingDialog.dismiss()
        }
    }

    private fun observeAddress() {
        locationViewModel.address.observe(viewLifecycleOwner) { res ->
            address = res
            val stringBuilder = StringBuilder()
                .append(if (address?.thoroughfare != null) "${address?.thoroughfare}, " else "")
                .append(if (address?.locality != null) "${address?.locality}" else "")

            binding.searchView.queryHint = stringBuilder
        }

        locationViewModel.geoCoderError.observe(viewLifecycleOwner) { error ->
            showShortToast(error)
        }
    }

    private fun observeUserLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { userLocation ->
            if (!userLocation.isNullOrEmpty()) {
                val location = userLocation.find { it.currentUse }
                location?.let {
                    map?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude), 17f
                        )
                    )
                }
            }
        }
    }

    private fun observeCurrentLocation() {
        locationViewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                locationViewModel.removeLocationUpdates()
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude), 17f))
            }
        }
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.searchSuggestionRv.isVisible) {
                        binding.searchView.setQuery("", false)
                    } else {
                        findNavController().popBackStack()
                    }
                }
            })
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
        locationViewModel.requestLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        view?.animation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                getMap()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::searchRequestTask.isInitialized) searchRequestTask.cancel()
        if (mapFragment != null) {
            val ft = childFragmentManager.beginTransaction()
            ft.remove(mapFragment!!)
        }
    }
}