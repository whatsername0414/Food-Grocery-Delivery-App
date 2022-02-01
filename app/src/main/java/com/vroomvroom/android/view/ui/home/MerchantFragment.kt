package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import coil.load
import com.google.android.material.tabs.TabLayout
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentMerchantBinding
import com.vroomvroom.android.domain.model.merchant.MerchantMapper
import com.vroomvroom.android.domain.model.product.ProductMapper
import com.vroomvroom.android.utils.OnProductClickListener
import com.vroomvroom.android.view.ui.home.adapter.CartAdapter
import com.vroomvroom.android.view.ui.home.adapter.ProductsSectionAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.activityviewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MerchantFragment : Fragment(), OnProductClickListener {

    @Inject lateinit var merchantMapper: MerchantMapper
    @Inject lateinit var productMapper: ProductMapper
    private val viewModel by viewModels<HomeViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val productsSectionAdapter by lazy { ProductsSectionAdapter(this) }
    private val cartAdapter by lazy { CartAdapter() }
    private val args: MerchantFragmentArgs by navArgs()

    private lateinit var binding: FragmentMerchantBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var isUserScrolling: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMerchantBinding.inflate(inflater)
        linearLayoutManager = binding.productSectionRv.layoutManager as LinearLayoutManager
        return binding.root
    }
//    .. / .-.. --- ...- . / -.-- --- ..- / .-. --- ... .
//    .- -. / -- .- -.-- / -... .-. --- -. ... .- .-..
    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ctlMerchant.setExpandedTitleColor(ContextCompat.getColor(requireContext(), R.color.white))
        viewModel.queryMerchant(args.id)

        //Tesla soon

        binding.productSectionRv.adapter = productsSectionAdapter

        //private functions
        observeMerchantLiveData()
        observeRoomCartItemLiveData()
        syncTabWithRecyclerView()
        observeIsCartCardViewVisible()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnInfo.setOnClickListener {
            findNavController().navigate(R.id.action_merchantFragment_to_merchantInfoFragment)
        }
        binding.merchantRetryButton.setOnClickListener {
            viewModel.queryMerchant(args.id)
            observeMerchantLiveData()
        }
        binding.toggleCart.setOnClickListener {
            findNavController().navigate(R.id.action_merchantFragment_to_cartBottomSheetFragment)
        }
        binding.btnMaximizeCartCardView.setOnClickListener {
            viewModel.isCartCardViewVisible.postValue(true)
        }
        binding.btnMinimizeCartCardView.setOnClickListener {
            viewModel.isCartCardViewVisible.postValue(false)
        }
        cartAdapter.onCartItemClicked = { cartItem ->
            viewModel.updateCartItem(cartItem)
        }
    }

    override fun onClick(product: MerchantQuery.Product?) {
        product?.let { prod ->
            val navArgs = productMapper.mapToDomainModel(prod)
            findNavController().navigate(MerchantFragmentDirections.actionMerchantFragmentToProductBottomSheetFragment(navArgs))
        }

    }

    private fun observeMerchantLiveData() {
        viewModel.merchant.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.merchantConnectionFailedNotice.visibility = View.GONE
                    binding.merchantAppBar.visibility = View.GONE
                    binding.productSectionRv.visibility = View.GONE
                    binding.cartCardVIew.visibility = View.GONE
                    binding.btnMaximizeCartCardView.visibility = View.GONE
                    binding.merchantShimmerLayout.startShimmer()
                    binding.merchantShimmerLayout.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    val merchant = response.result.getMerchant
                    activityViewModel.merchant = merchant
                    productsSectionAdapter.submitList(merchant.product_sections)
                    initializeTabItem(merchant.product_sections)
                    dataBinder(merchant)
                    binding.merchantShimmerLayout.visibility = View.GONE
                    binding.merchantShimmerLayout.stopShimmer()
                    binding.merchantAppBar.visibility = View.VISIBLE
                    binding.productSectionRv.visibility = View.VISIBLE
                }
                is ViewState.Error -> {
                    binding.merchantShimmerLayout.visibility = View.GONE
                    binding.merchantShimmerLayout.stopShimmer()
                    binding.merchantAppBar.visibility = View.GONE
                    binding.productSectionRv.visibility = View.GONE
                    binding.cartCardVIew.visibility = View.GONE
                    binding.btnMaximizeCartCardView.visibility = View.GONE
                    binding.merchantConnectionFailedNotice.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeRoomCartItemLiveData() {
        viewModel.cartItem.observe(viewLifecycleOwner, { items ->
            if (items.isNotEmpty()) {
                var subTotal = 0.0
                items.forEach { item ->
                    subTotal += item.cartItemEntity.price
                }
                binding.cartFabDetail.text = getString(R.string.cart_fab_detail, items.size, "%.2f".format(subTotal))
                viewModel.isCartCardViewVisible.postValue(true)
                cartAdapter.submitList(items)
            } else {
                viewModel.isCartCardViewVisible.postValue(false)
                binding.btnMaximizeCartCardView.visibility = View.GONE
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun dataBinder(merchant: MerchantQuery.GetMerchant?) {
        binding.ctlMerchant.title = merchant?.name
        binding.ratingBar.rating = merchant?.ratings?.toFloat() ?: 0f
        binding.closingTv.text = "Open until ${merchant?.closing} PM"
        binding.merchantRating.text = if (merchant?.rates != null) "${merchant.ratings} (${merchant.rates} ${if (merchant.rates == 1) "rating" else "ratings"})" else "0.0"
        binding.merchantImg.load(merchant?.img_url)
    }

    private fun observeIsCartCardViewVisible() {
        viewModel.isCartCardViewVisible.observe(viewLifecycleOwner, { isVisible ->
            val transition = Slide(Gravity.END)
            transition.duration = 400
            transition.addTarget(binding.cartCardVIew)

            TransitionManager.beginDelayedTransition(binding.root, transition)
            if (isVisible) {
                binding.cartCardVIew.visibility = View.VISIBLE
                binding.btnMaximizeCartCardView.visibility = View.VISIBLE
                binding.btnMaximizeCartCardView.animate().alpha(0f).duration = 300
            } else {
                binding.cartCardVIew.visibility = View.GONE
                binding.btnMaximizeCartCardView.animate().alpha(1f).duration = 400
            }
        })
    }

    private fun initializeTabItem(products: List<MerchantQuery.Product_section?>?) {
        binding.tlMerchant.removeAllTabs()
        products?.forEach { product ->
            binding.tlMerchant.addTab(binding.tlMerchant.newTab().setText(product?.name))
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
}

@BindingAdapter("bottomSheetProductImageUrl")
fun setBottomSheetProductImageUrl(imageView: ImageView, url: String?) {
    if (url != null) {
        imageView.load(url)
    }
}