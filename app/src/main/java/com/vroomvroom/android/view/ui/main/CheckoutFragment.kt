package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCheckoutBinding
import com.vroomvroom.android.view.adapter.CheckoutAdapter
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Utils
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.AuthViewModel
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CheckoutFragment : Fragment(), OnMapReadyCallback {

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()
    private val checkoutAdapter by lazy { CheckoutAdapter() }

    private lateinit var navController: NavController
    private lateinit var binding: FragmentCheckoutBinding

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private var coordinates: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckoutBinding.inflate(inflater)
        navController = findNavController()
        mapView = binding.userLocationMapView
        initGoogleMap(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.checkoutToolbar.setupWithNavController(navController, appBarConfiguration)

        binding.checkoutRv.adapter = checkoutAdapter

        //private functions
        observeCurrentUser()
        observeRoomCartItemLiveData()
        observeLocation()
        observePaymentMethod()

        authViewModel.idToken.observe(viewLifecycleOwner, { result ->
            when (result) {
                is ViewState.Success -> Log.d("CheckoutFragment", result.result)
                is ViewState.Error -> Log.d("CheckoutFragment", result.exception.message.toString())
                else -> Log.d("CheckoutFragment", result.toString())
            }
        })

        binding.btnAddAddress.setOnClickListener {
            navController.navigate(R.id.action_checkoutFragment_to_mapsFragment)
        }

        binding.editPaymentMethod.setOnClickListener {
            navController.navigate(R.id.action_checkoutFragment_to_paymentMethodFragment)
        }
    }

    private fun observeCurrentUser() {
        authViewModel.currentUser.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Success -> {
                    binding.btnPlaceOrder.isEnabled = true
                }
                is ViewState.Error -> {
                    navController.navigate(R.id.action_checkoutFragment_to_authBottomSheetFragment)
                    binding.btnPlaceOrder.setOnClickListener {
                        navController.navigate(R.id.action_checkoutFragment_to_authBottomSheetFragment)
                    }
                }
                else -> Log.d("CheckoutFragment", response.toString())
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun observeRoomCartItemLiveData() {
        mainViewModel.cartItem.observe(viewLifecycleOwner, { items ->
            checkoutAdapter.submitList(items)
            var subTotal = 0
            items.forEach { item ->
                subTotal += item.cartItem.price
            }
            binding.checkoutMerchant.text = items.first().cartItem.merchant
            binding.checkoutSubTotalTv.text = "₱${subTotal}.00"
            binding.btnPlaceOrder.text = "Place Order • ₱${subTotal + 49}.00"
        })
    }

    private fun observeLocation() {
        mainViewModel.location.observe(viewLifecycleOwner, {
            it?.let { location ->
                coordinates = Utils.initLocation(location)
                val address = coordinates?.let { latLong ->
                    Utils.customGeoCoder(latLong, requireContext())
                }
                updateViewOnAddressReady(address)
            }
        })
    }

    private fun observePaymentMethod() {
        mainViewModel.paymentMethod.observe(viewLifecycleOwner, { method ->
            when (method) {
                "Cash" -> {
                    binding.imgMethod.setImageResource(R.drawable.ic_money)
                    binding.paymentMethod.text = method
                }
                "GCash" -> {
                    binding.imgMethod.setImageResource(R.drawable.gcash)
                    binding.paymentMethod.text = method
                }
            }
        })
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY)
        }
        mapView?.onCreate(mapViewBundle)
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val userLocation = coordinates ?: LatLng(13.3590302,123.7298568)
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
        map?.uiSettings?.setAllGesturesEnabled(false)
        map?.addMarker(MarkerOptions().position(userLocation).icon(bitmapDescriptorFromVector(R.drawable.ic_location)))
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15.8f))
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(requireContext(), vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun updateViewOnAddressReady(address: Address?) {
        mainViewModel.address.postValue(address)
        binding.checkoutAddress.text =
            address?.thoroughfare ?: "Street not provided"
        binding.checkoutCity.text =
            address?.locality ?: "City not provided"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}