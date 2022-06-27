package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentMerchantBinding
import com.vroomvroom.android.data.model.merchant.Merchant
import com.vroomvroom.android.data.model.merchant.Product
import com.vroomvroom.android.data.model.merchant.ProductSections
import com.vroomvroom.android.utils.OnProductClickListener
import com.vroomvroom.android.utils.Utils.onReady
import com.vroomvroom.android.utils.Utils.stringBuilder
import com.vroomvroom.android.utils.Utils.timeFormatter
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.home.adapter.CartAdapter
import com.vroomvroom.android.view.ui.home.adapter.ProductSectionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MerchantFragment : BaseFragment<FragmentMerchantBinding>(
    FragmentMerchantBinding::inflate
), OnProductClickListener {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private val productSectionAdapter by lazy { ProductSectionAdapter(this) }
    private val cartAdapter by lazy { CartAdapter() }
    private val args: MerchantFragmentArgs by navArgs()
    private var isUserScrolling: Boolean = false
    /** .. / .-.. --- ...- . / -.-- --- ..- / .-. --- ... .
    .- -. / -- .- -.-- / -... .-. --- -. ... .- .-.. **/
    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        linearLayoutManager = binding.productSectionRv.layoutManager as LinearLayoutManager


        //Tesla soon
        binding.productSectionRv.adapter = productSectionAdapter

        //private functions
        observeMerchantLiveData()
        syncTabWithRecyclerView()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnInfo.setOnClickListener {
            findNavController().navigate(R.id.action_merchantFragment_to_merchantInfoFragment)
        }
        binding.btnToggleCart.setOnClickListener {
            findNavController().navigate(R.id.action_merchantFragment_to_cartBottomSheetFragment)
        }
        binding.btnMaximizeCartCardView.setOnClickListener {
            homeViewModel.isCartCardViewVisible.postValue(true)
        }
        binding.btnMinimizeBtnToggleCart.setOnClickListener {
            homeViewModel.isCartCardViewVisible.postValue(false)
        }
        cartAdapter.onCartItemClicked = { cartItem ->
            homeViewModel.updateCartItem(cartItem)
        }

    }

    override fun onClick(product: Product) {
        findNavController().navigate(
            MerchantFragmentDirections.actionMerchantFragmentToProductBottomSheetFragment(product))
    }

    private fun observeMerchantLiveData() {
        homeViewModel.merchant.observe(viewLifecycleOwner) { response ->
            when(response) {
                is Resource.Loading -> {
                    binding.commonNoticeLayout.hideNotice()
                    binding.merchantAppBar.visibility = View.GONE
                    binding.productSectionRv.visibility = View.GONE
                    binding.merchantShimmerLayout.visibility = View.VISIBLE
                    binding.merchantShimmerLayout.startShimmer()
                }
                is Resource.Success -> {
                    val merchant = response.data
                    mainActivityViewModel.merchant = merchant
                    productSectionAdapter.submitList(merchant.productSections)
                    initializeTabItem(merchant.productSections)
                    dataBinder(merchant)
                    binding.commonNoticeLayout.hideNotice()
                    binding.merchantShimmerLayout.visibility = View.GONE
                    binding.merchantShimmerLayout.stopShimmer()
                    binding.merchantAppBar.visibility = View.VISIBLE
                    binding.productSectionRv.apply {
                        visibility = View.VISIBLE
                        onReady {
                            observeRoomCartItemLiveData()
                            observeIsCartCardViewVisible()
                        }
                    }
                }
                is Resource.Error -> {
                    binding.merchantShimmerLayout.visibility = View.GONE
                    binding.merchantShimmerLayout.stopShimmer()
                    binding.merchantAppBar.visibility = View.GONE
                    binding.productSectionRv.visibility = View.GONE
                    binding.commonNoticeLayout.showNetworkError {
                        homeViewModel.getMerchant(args.id)
                        observeMerchantLiveData()
                    }
                }
            }
        }
    }

    private fun observeRoomCartItemLiveData() {
        homeViewModel.cartItem.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                var subTotal = 0.0
                items.forEach { item ->
                    subTotal += item.cartItem.price
                }
                binding.btnToggleCart.text =
                    getString(R.string.cart_fab_detail, items.size, "%.2f".format(subTotal))
                homeViewModel.isCartCardViewVisible.postValue(true)
                cartAdapter.submitList(items)
            } else {
                homeViewModel.isCartCardViewVisible.postValue(false)
                binding.btnMaximizeCartCardView.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun dataBinder(merchant: Merchant) {
        binding.ctlMerchant.title = merchant.name
        binding.categoriesTv.text = merchant.categories.stringBuilder()
        binding.ratingBar.rating = merchant.ratings?.toFloat() ?: 0f
        binding.timeTv.text = getString(
            R.string.time, timeFormatter(merchant.opening), timeFormatter(merchant.closing))
        binding.merchantRating.text =
            if (merchant.rates != null)
                "${merchant.ratings} (${merchant.rates} ${if (merchant.rates == 1) "Review" 
                else "Reviews"})" else "0.0"
        Glide
            .with(this)
            .load(merchant.img_url)
            .placeholder(R.drawable.ic_placeholder)
            .into(binding.merchantImg)
    }

    private fun observeIsCartCardViewVisible() {
        homeViewModel.isCartCardViewVisible.observe(viewLifecycleOwner) { isVisible ->
            val transition = Slide(Gravity.END)
            transition.duration = 400
            transition.addTarget(binding.cartLayout)

            TransitionManager.beginDelayedTransition(binding.root, transition)
            if (isVisible) {
                binding.cartLayout.visibility = View.VISIBLE
                binding.btnMaximizeCartCardView.visibility = View.VISIBLE
                binding.btnMaximizeCartCardView.animate().alpha(0f).duration = 300
            } else {
                binding.cartLayout.visibility = View.GONE
                binding.btnMaximizeCartCardView.animate().alpha(1f).duration = 400
            }
        }
    }

    private fun initializeTabItem(products: List<ProductSections>?) {
        binding.tlMerchant.removeAllTabs()
        products?.forEach { product ->
            binding.tlMerchant.addTab(binding.tlMerchant.newTab().setText(product.name))
        }
    }


    private fun syncTabWithRecyclerView() {
        // Move recyclerview to the selected position
        binding.tlMerchant.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (!isUserScrolling) {
                    val position = tab.position
                    smoothScroller(position)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        // Detect recyclerview position and select tab respectively.
        binding.productSectionRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserScrolling = true
                }  else if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    isUserScrolling = false
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isUserScrolling) {
                    val firstCompletePos = linearLayoutManager.findFirstVisibleItemPosition()

                    if (firstCompletePos != binding.tlMerchant.selectedTabPosition)
                        binding.tlMerchant.getTabAt(firstCompletePos)?.select()
                }
            }
        })
    }

    private fun smoothScroller(position: Int) {
        val smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        smoothScroller.targetPosition = position
        linearLayoutManager.startSmoothScroll(smoothScroller)
    }

    override fun onResume() {
        super.onResume()
        view?.animation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                homeViewModel.getMerchant(args.id)
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}