package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.domain.model.order.*
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Utils.setMap
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import com.vroomvroom.android.view.ui.home.adapter.CheckoutAdapter
import com.vroomvroom.android.view.ui.home.viewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.home.viewmodel.CheckoutViewModel
import com.vroomvroom.android.view.ui.location.viewmodel.LocationViewModel
import com.vroomvroom.android.view.ui.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CheckoutFragment : Fragment(), OnMapReadyCallback {

    @Inject lateinit var orderInputBuilder: OrderInputBuilder
    @Inject lateinit var orderInputMapper: OrderInputMapper
    private val locationViewModel by viewModels<LocationViewModel>()
    private val checkoutViewModel by viewModels<CheckoutViewModel>()
    private val mainViewModel by viewModels<HomeViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()
    private val checkoutAdapter by lazy { CheckoutAdapter() }
    private val loadingDialog by lazy { LoadingDialog(requireActivity()) }

    private lateinit var binding: FragmentCheckoutBinding
    private lateinit var navController: NavController

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
        binding.success.visibility = View.GONE

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.checkoutRv.adapter = checkoutAdapter

        //private functions
        observeUserRecord()
        observeRoomCartItemLiveData()
        observePaymentMethod()
        observeOrderLiveData()

        binding.btnAddAddress.setOnClickListener {
            navController.navigate(R.id.action_checkoutFragment_to_addressesFragment)
        }

        binding.editPaymentMethod.setOnClickListener {
            navController.navigate(R.id.action_checkoutFragment_to_paymentMethodFragment)
        }
    }

    private fun observeUserRecord() {
        authViewModel.userRecord.observe(viewLifecycleOwner, { user ->
            when {
                user.isEmpty() -> {
                    binding.customBtnPlaceOrderTv.text = getString(R.string.login_or_sign_up)
                    binding.customBtnPlaceOrder.setOnClickListener {
                        navController.navigate(R.id.action_checkoutFragment_to_authBottomSheetFragment)
                    }
                }
                user.first().phone_number.isNullOrBlank() -> {
                    binding.customBtnPlaceOrderTv.text = getString(R.string.add_mobile_number)
                    binding.customBtnPlaceOrder.setOnClickListener {
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
            items.forEach { item ->
                checkoutViewModel.subtotal += item.cartItemEntity.price
            }
            binding.checkoutMerchant.text = items.first().cartItemEntity.merchant.merchant_name
            binding.checkoutSubTotalTv.text = "₱${"%.2f".format(checkoutViewModel.subtotal)}"
        })
    }

    private fun observeLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner, { userLocation ->
            locationEntity = userLocation.find { it.current_use }
            locationEntity?.let {
                updateLocationViews(it)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun observeIsLocationConfirmed() {
        checkoutViewModel.isLocationConfirmed.observe(viewLifecycleOwner, { confirmed ->
            if (!confirmed) {
                binding.customBtnPlaceOrderTv.text = getString(R.string.confirm_address)
                binding.customBtnPlaceOrder.setOnClickListener {
                    locationEntity?.let {
                        checkoutViewModel.mutationUpdateUserLocation(it)
                    }
                }
            } else {
                binding.customBtnPlaceOrderTv.text = "Place Order • ₱${"%.2f".format(checkoutViewModel.subtotal + 49)}.00"
                binding.customBtnPlaceOrder.setOnClickListener {
                    checkoutViewModel.mutationCreateOrder(
                        orderInputMapper.mapToDomainModel(orderInputBuilder())
                    )
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun observeOrderLiveData() {
        checkoutViewModel.order.observe(viewLifecycleOwner, { response ->
            when(response) {
                is ViewState.Loading -> {
                    loadingDialog.show()
                }
                is ViewState.Success -> {
                    loadingDialog.showSuccess()
                    mainViewModel.cartItem.removeObservers(viewLifecycleOwner)
                    mainViewModel.deleteAllCartItem()
                    binding.customBtnPlaceOrderTv.text = getString(R.string.continue_shopping)
                    binding.customBtnPlaceOrder.setOnClickListener {
                        navController.navigate(R.id.action_checkoutFragment_to_homeFragment)
                    }
                }
                is ViewState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Unable to place your order",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.customBtnPlaceOrderTv.text = "Place Order • ₱${"%.2f".format(checkoutViewModel.subtotal + 49)}"
                }
            }
        })
    }

    private fun orderInputBuilder(): Order {
        val cartItems = mainViewModel.cartItem.value!!
        val merchant = cartItems.first().cartItemEntity.merchant
        return orderInputBuilder.builder(
            Payment(
                activityViewModel.paymentMethod.value ?: "Cash On Delivery",
                null
            ),
            49.00,
            checkoutViewModel.subtotal,
            merchant.merchant_id,
            cartItems
        )
    }

    private fun observePaymentMethod() {
        activityViewModel.paymentMethod.observe(viewLifecycleOwner, { method ->
            when (method) {
                "Cash On Delivery" -> {
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
        mapView?.onDestroy()
    }
}