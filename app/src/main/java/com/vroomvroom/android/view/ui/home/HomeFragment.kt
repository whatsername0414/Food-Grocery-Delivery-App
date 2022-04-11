package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentHomeBinding
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.domain.model.merchant.MerchantData
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.utils.Utils.updateAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import com.vroomvroom.android.view.ui.home.adapter.CategoryAdapter
import com.vroomvroom.android.view.ui.home.adapter.MerchantAdapter
import com.vroomvroom.android.view.ui.activityviewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.location.viewmodel.LocationViewModel
import com.vroomvroom.android.view.ui.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
class HomeFragment: Fragment() {

    private val homeViewModel by viewModels<HomeViewModel>()
    private val locationViewModel by viewModels<LocationViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val categoryAdapter by lazy { CategoryAdapter() }
    private val merchantAdapter by lazy { MerchantAdapter(false) }
    private var snackBar: Snackbar? = null

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fetchProgress.visibility = View.GONE

        homeViewModel.queryCategory()
        homeViewModel.queryMerchants()

        observeUser()
        observeUserLocation()
        observeCategory()
        observeMerchants()
        observeRoomCartItem()

        binding.categoryRv.adapter = categoryAdapter
        binding.merchantRv. adapter = merchantAdapter
        ViewCompat.setNestedScrollingEnabled(binding.merchantRv, false)

        binding.locationCv.setOnClickListener {
            findNavController().safeNavigate(
                HomeFragmentDirections.actionHomeFragmentToLocationBottomSheetFragment()
            )
        }

        binding.favorite.setOnClickListener {
            findNavController().safeNavigate(
                HomeFragmentDirections.actionHomeFragmentToFavoriteFragment()
            )
        }

        binding.cart.setOnClickListener {
            findNavController().safeNavigate(
                HomeFragmentDirections.actionHomeFragmentToCartBottomSheetFragment()
            )
        }

        categoryAdapter.onCategoryClicked = { category ->
            homeViewModel.categoryClicked = true
            if (category?.name != null) {
                homeViewModel.queryMerchantsByCategory(category.name)
            } else {
                homeViewModel.queryMerchantsByCategory("")
            }
        }

        merchantAdapter.onMerchantClicked = { merchant ->
            if (merchant._id.isNotBlank()) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMerchantFragment(merchant._id)
                )
            }
        }

        merchantAdapter.onFavoriteClicked = { merchant, direction ->
            homeViewModel.favorite(merchant._id, direction)
            activityViewModel.favoriteDirection = direction
            observeFavorite(merchant)
        }

        binding.btnRetry.setOnClickListener {
            homeViewModel.queryCategory()
            homeViewModel.queryMerchants()
        }
    }

    private fun observeUser() {
        authViewModel.userRecord.observe(viewLifecycleOwner, { users ->
            if (!users.isNullOrEmpty()) {
                binding.favorite.visibility = View.VISIBLE
                merchantAdapter.setUser(users.first())
                merchantAdapter.notifyDataSetChanged()
            } else {
                binding.favorite.visibility = View.GONE
                merchantAdapter.setUser(null)
                merchantAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun observeUserLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner, { userLocation ->
            if (userLocation.isNullOrEmpty()) {
                findNavController().navigate(R.id.action_homeFragment_to_locationFragment)
            } else {
                val location = userLocation.find { it.current_use }
                location?.let { updateLocationTextView(it) }
            }
        })
    }
    private fun updateLocationTextView(locationEntity: UserLocationEntity) {
        binding.addressTv.text =
            locationEntity.address ?: getString(R.string.street_not_provided)
        binding.cityTv.text =
            locationEntity.city ?: getString(R.string.city_not_provided)
    }

    private fun observeCategory() {
        homeViewModel.category.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.apply {
                        connectionFailedLayout.visibility = View.GONE
                        title.visibility = View.GONE
                        categoryRv.visibility = View.GONE
                        categoryShimmerLayout.apply {
                            visibility = View.VISIBLE
                            startShimmer()
                        }
                    }
                }
                is ViewState.Success -> {
                    val category = response.result.getCategories
                    categoryAdapter.submitList(category)
                    binding.apply {
                        title.visibility = View.VISIBLE
                        categoryRv.visibility = View.VISIBLE
                        categoryShimmerLayout.apply {
                            stopShimmer()
                            visibility = View.INVISIBLE
                        }
                    }
                }
                is ViewState.Error -> {
                    categoryAdapter.submitList(emptyList())
                    binding.apply {
                        categoryRv.visibility = View.GONE
                        title.visibility = View.GONE
                        connectionFailedLayout.visibility = View.VISIBLE
                        categoryShimmerLayout.apply {
                            stopShimmer()
                            visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun observeMerchants() {
        homeViewModel.merchants.observe(viewLifecycleOwner, { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.apply {
                        connectionFailedLayout.visibility = View.GONE
                        if (homeViewModel.categoryClicked) {
                            fetchProgress.visibility = View.VISIBLE
                        } else {
                            merchantsShimmerLayout.apply {
                                visibility = View.VISIBLE
                                startShimmer()
                            }
                        }
                    }
                }
                is ViewState.Success -> {
                    val merchant = response.result.data
                    merchantAdapter.setData(merchant)
                    binding.apply {
                        fetchProgress.visibility = View.GONE
                        merchantsShimmerLayout.apply {
                            visibility = View.GONE
                            stopShimmer()
                        }
                    }
                }
                is ViewState.Error -> {
                    merchantAdapter.setData(mutableListOf())
                    binding.apply {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                        merchantRv.visibility = View.GONE
                        fetchProgress.visibility = View.GONE
                        merchantsShimmerLayout.apply {
                            visibility = View.GONE
                            stopShimmer()
                        }
                    }
                }
            }
        })
    }

    private fun observeFavorite(merchant: MerchantData) {
        homeViewModel.favorite.observe(viewLifecycleOwner, { response ->
            val direction = activityViewModel.favoriteDirection
            when(response) {
                is ViewState.Loading -> Unit
                is ViewState.Success -> {
                    if (direction == 1) {
                        showSnackBar("Added to favorites")
                        snackBar?.setAction(R.string.view_all) {
                            findNavController().navigate(R.id.action_homeFragment_to_favoriteFragment)
                        }?.show()
                        merchantAdapter.updateAdapter(merchant, true)
                    } else {
                        showSnackBar("Removed from favorites")
                        snackBar?.show()
                        merchantAdapter.updateAdapter(merchant, false)
                    }
                    homeViewModel.favorite.removeObservers(viewLifecycleOwner)
                }
                is ViewState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (direction == 1) {
                        merchantAdapter.updateAdapter(merchant, true)
                    } else {
                        merchantAdapter.updateAdapter(merchant, false)
                    }
                    homeViewModel.favorite.removeObservers(viewLifecycleOwner)
                }
            }
        })
    }

    private fun observeRoomCartItem() {
        homeViewModel.cartItem.observe(viewLifecycleOwner, { items ->
            if (items.isNullOrEmpty()) {
                binding.cardCartCounter.visibility = View.GONE
            } else {
                binding.cartCounter.text = "${items.size}"
                binding.cardCartCounter.visibility = View.VISIBLE
            }
        })
    }

    private fun showSnackBar(label: String) {
        snackBar = Snackbar.make(
            binding.root,
            label,
            Snackbar.LENGTH_LONG
        )
        val snackBarView = snackBar?.view
        snackBarView?.translationY = -(448 / requireContext().resources.displayMetrics.density)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackBar?.dismiss()
    }
}