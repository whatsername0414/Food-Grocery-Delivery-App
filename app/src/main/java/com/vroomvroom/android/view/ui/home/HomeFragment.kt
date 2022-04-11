package com.vroomvroom.android.view.ui.home

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentHomeBinding
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.utils.Constants.ALL
import com.vroomvroom.android.utils.Constants.BY_CATEGORY
import com.vroomvroom.android.utils.Constants.SCROLL_THRESHOLD
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.home.adapter.CategoryAdapter
import com.vroomvroom.android.view.ui.home.adapter.MerchantAdapter
import com.vroomvroom.android.view.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
@SuppressLint("NotifyDataSetChanged")
class HomeFragment: BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
) {

    private val categoryAdapter by lazy { CategoryAdapter() }
    private val merchantAdapter by lazy { MerchantAdapter() }
    private var categoriesLoaded = false
    private var merchantsLoaded = false
    private var categoryClicked = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUser()
        observeCategories()
        observeMerchants()
        observeUserLocation()
        observeRoomCartItem()
        viewTreeObserver()
        shouldBackToTopObserver()

        binding.categoryRv.adapter = categoryAdapter
        binding.merchantRv. adapter = merchantAdapter

        binding.addressLayout.setOnClickListener {
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
            categoryClicked = true
            if (category != null) {
                mainViewModel.queryMerchants(BY_CATEGORY, category)
                binding.shopsTitle.text = category

            } else {
                mainViewModel.queryMerchants(ALL, null)
                binding.shopsTitle.text = getString(R.string.all_shops)
            }
        }

        merchantAdapter.onMerchantClicked = { merchant ->
            categoryClicked = false
            if (merchant._id.isNotBlank()) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMerchantFragment(merchant._id)
                )
            }
        }

        merchantAdapter.apply {
            onFavoriteClicked = { merchant, position, direction ->
                homeViewModel.favorite(merchant._id, direction)
                observeFavorite(this, merchant, position, direction)
            }
        }
    }

    private fun observeUser() {
        authViewModel.userRecord.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.favorite.visibility = View.VISIBLE
                merchantAdapter.setUser(user)
                merchantAdapter.notifyDataSetChanged()
            } else {
                binding.favorite.visibility = View.GONE
                merchantAdapter.setUser(null)
                merchantAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun observeUserLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { userLocation ->
            if (userLocation.isNullOrEmpty()) {
                findNavController().navigate(R.id.action_homeFragment_to_locationFragment)
            } else {
                val location = userLocation.find { it.currentUse }
                location?.let { updateLocationTextView(it) }
            }
        }
    }
    private fun updateLocationTextView(locationEntity: UserLocationEntity) {
        binding.addressTv.text =
            locationEntity.address ?: getString(R.string.street_not_provided)
        binding.cityTv.text =
            locationEntity.city ?: getString(R.string.city_not_provided)
    }

    private fun observeCategories() {
        mainViewModel.categories.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.apply {
                        commonNoticeLayout.hideNotice()
                        homeRvLinearLayout.visibility = View.GONE
                        categoryShimmerLayout.visibility = View.VISIBLE
                        categoryShimmerLayout.startShimmer()

                    }
                }
                is ViewState.Success -> {
                    categoriesLoaded = true
                    val category = response.result.getCategories
                    categoryAdapter.submitList(category)
                    binding.apply {
                        if (categoriesLoaded && merchantsLoaded) {
                            categoryShimmerLayout.stopShimmer()
                            merchantsShimmerLayout.stopShimmer()
                            categoryShimmerLayout.visibility = View.GONE
                            merchantsShimmerLayout.visibility = View.GONE
                            homeRvLinearLayout.visibility = View.VISIBLE
                        }
                    }
                }
                is ViewState.Error -> {
                    categoriesLoaded = false
                    binding.apply {
                        categoryShimmerLayout.stopShimmer()
                        merchantsShimmerLayout.stopShimmer()
                        categoryShimmerLayout.visibility = View.GONE
                        merchantsShimmerLayout.visibility = View.GONE
                        homeRvLinearLayout.visibility = View.VISIBLE
                    }
                    binding.commonNoticeLayout.showNetworkError {
                        mainViewModel.queryCategory("main")
                        mainViewModel.queryMerchants(ALL, null)
                    }
                }
            }
        }
    }

    private fun observeMerchants() {
        mainViewModel.merchants.observe(viewLifecycleOwner) { response ->
            lifecycleScope.launch(Dispatchers.Main) {
                when (response) {
                    is ViewState.Loading -> {
                        binding.apply {
                            commonNoticeLayout.hideNotice()
                            if (categoryClicked) {
                                fetchProgress.visibility = View.VISIBLE
                            } else {
                                homeRvLinearLayout.visibility = View.GONE
                                merchantsShimmerLayout.visibility = View.VISIBLE
                                merchantsShimmerLayout.startShimmer()
                            }
                        }
                    }
                    is ViewState.Success -> {
                        merchantsLoaded = true
                        val merchants = response.result.data
                        merchantAdapter.submitList(checkForChanges(merchants))
                        binding.apply {
                            if (categoryClicked) {
                                fetchProgress.visibility = View.GONE
                            } else {
                                if (categoriesLoaded && merchantsLoaded) {
                                    categoryShimmerLayout.stopShimmer()
                                    merchantsShimmerLayout.stopShimmer()
                                    categoryShimmerLayout.visibility = View.GONE
                                    merchantsShimmerLayout.visibility = View.GONE
                                    homeRvLinearLayout.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    is ViewState.Error -> {
                        merchantsLoaded = false
                        binding.apply {
                            merchantsShimmerLayout.apply {
                                visibility = View.GONE
                                stopShimmer()
                            }
                            homeRvLinearLayout.visibility = View.GONE
                            fetchProgress.visibility = View.GONE
                            commonNoticeLayout.showNetworkError {
                                mainViewModel.queryCategory("main")
                                mainViewModel.queryMerchants(ALL, null)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun viewTreeObserver() {
        binding.homeNestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            mainActivityViewModel.isHomeScrolled.postValue(
                scrollY > SCROLL_THRESHOLD
            )
        }
    }

    private fun shouldBackToTopObserver() {
        mainActivityViewModel.shouldBackToTop.observe(viewLifecycleOwner) { shouldBackToTop ->
            if (shouldBackToTop) {
                binding.homeNestedScrollView.apply {
                    scrollBy(0, 1)
                    ObjectAnimator.ofInt(
                        this,
                        "scrollY",
                        binding.root.top
                    ).setDuration(400).start()
                }
                mainActivityViewModel.shouldBackToTop.postValue(false)
            }
        }
    }

    private fun checkForChanges(merchants: MutableList<Merchant?>): MutableList<Merchant?> {
        mainActivityViewModel.favoritesChanges.forEach { (_, v) ->
            val index = merchants.indexOf(merchants.find { it?._id == v._id })
            merchants[index] = v
        }
        return merchants
    }

    private fun observeRoomCartItem() {
        homeViewModel.cartItem.observe(viewLifecycleOwner) { items ->
            if (items.isNullOrEmpty()) {
                binding.cartCounter.visibility = View.GONE
            } else {
                binding.cartCounter.apply {
                    text = "${items.size}"
                    visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (view?.animation != null) {
            view?.animation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    if (mainViewModel.merchants.value == null &&
                        mainViewModel.categories.value == null) {
                        mainViewModel.queryCategory("main")
                        mainViewModel.queryMerchants(ALL, null)
                    }
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        } else {
            if (mainViewModel.merchants.value == null && mainViewModel.categories.value == null) {
                mainViewModel.queryCategory("main")
                mainViewModel.queryMerchants(ALL, null)
            }
        }
    }
}