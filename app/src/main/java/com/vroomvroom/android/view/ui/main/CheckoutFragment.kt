package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCheckoutBinding
import com.vroomvroom.android.domain.db.UserLocationEntity
import com.vroomvroom.android.domain.model.order.CreateOrder
import com.vroomvroom.android.domain.model.order.OrderInputBuilder
import com.vroomvroom.android.domain.model.order.OrderInputMapper
import com.vroomvroom.android.domain.model.order.Payment
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Utils.setMap
import com.vroomvroom.android.view.adapter.CheckoutAdapter
import com.vroomvroom.android.viewmodel.AuthViewModel
import com.vroomvroom.android.viewmodel.LocationViewModel
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CheckoutFragment : Fragment(), OnMapReadyCallback {

    @Inject lateinit var orderInputBuilder: OrderInputBuilder
    @Inject lateinit var orderInputMapper: OrderInputMapper
    private val locationViewModel by viewModels<LocationViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()
    private val checkoutAdapter by lazy { CheckoutAdapter() }

    private lateinit var navController: NavController
    private lateinit var binding: FragmentCheckoutBinding

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private var locationEntity: UserLocationEntity? = null

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
        binding.checkoutProgress.visibility = View.GONE

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.checkoutToolbar.setupWithNavController(navController, appBarConfiguration)

        binding.checkoutRv.adapter = checkoutAdapter

        //private functions
        observeUserRecord()
        observeRoomCartItemLiveData()
        observePaymentMethod()

        binding.btnAddAddress.setOnClickListener {
            navController.navigate(R.id.action_checkoutFragment_to_mapsFragment)
        }

        binding.editPaymentMethod.setOnClickListener {
            navController.navigate(R.id.action_checkoutFragment_to_paymentMethodFragment)
        }
    }

    private fun observeUserRecord() {
        authViewModel.userRecord.observe(viewLifecycleOwner, { user ->
            when {
                user.isEmpty() -> {
                    binding.btnPlaceOrder.text = getString(R.string.login_or_sign_up)
                    binding.btnPlaceOrder.setOnClickListener {
                        navController.navigate(R.id.action_checkoutFragment_to_authBottomSheetFragment)
                    }
                }
                user.first().phone_number.isNullOrBlank() -> {
                    binding.btnPlaceOrder.text = getString(R.string.add_mobile_number)
                    binding.btnPlaceOrder.setOnClickListener {
                        navController.navigate(R.id.action_checkoutFragment_to_phoneVerificationFragment)
                    }
                }
                else -> {
                    observeIsLocationConfirmed()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun observeRoomCartItemLiveData() {
        mainViewModel.cartItem.observe(viewLifecycleOwner, { items ->
            checkoutAdapter.submitList(items)
            if (!mainViewModel.isSubtotalComputed) {
                items.forEach { item ->
                    mainViewModel.subtotal += item.cartItemEntity.price
                }
                mainViewModel.isSubtotalComputed = true
            }
            binding.checkoutMerchant.text = items.first().cartItemEntity.merchant.merchant_name
            binding.checkoutSubTotalTv.text = "₱${mainViewModel.subtotal}.00"
        })
    }

    private fun observeLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner, { userLocation ->
            locationEntity = userLocation.first()
            locationEntity?.let {
                updateLocationViews(it)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun observeIsLocationConfirmed() {
        mainViewModel.isLocationConfirmed.observe(viewLifecycleOwner, { confirmed ->
            if (!confirmed) {
                binding.btnPlaceOrder.text = getString(R.string.confirm_address)
                binding.btnPlaceOrder.setOnClickListener {
                    locationEntity?.let {
                        mainViewModel.mutationUpdateUserLocation(it)
                    }
                }
            } else {
                binding.btnPlaceOrder.text = "Place Order • ₱${mainViewModel.subtotal + 49}.00"
                binding.btnPlaceOrder.setOnClickListener {
                    mainViewModel.createOrder(
                        orderInputMapper.mapToDomainModel(orderInputBuilder())
                    )
                }
            }
        })
    }

    private fun orderInputBuilder(): CreateOrder {
        val cartItems = mainViewModel.cartItem.value!!
        val merchant = cartItems.first().cartItemEntity.merchant
        return orderInputBuilder.builder(
            Payment(
                mainViewModel.paymentMethod.value ?: "Cash",
                null
            ),
            49,
            mainViewModel.subtotal,
            merchant.merchant_id,
            merchant.merchant_name,
            cartItems
        )
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
        observeLocation()
    }

    private fun updateLocationViews(locationEntity: UserLocationEntity) {
        val coordinates = LatLng(locationEntity.latitude, locationEntity.longitude)
        map.setMap(requireContext(), coordinates)
        binding.checkoutAddress.text =
            locationEntity.address ?: getString(R.string.street_not_provided)
        binding.checkoutCity.text =
            locationEntity.city ?: getString(R.string.city_not_provided)
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
        mainViewModel.orderProductInput.clear()
        mapView?.onDestroy()
    }
}