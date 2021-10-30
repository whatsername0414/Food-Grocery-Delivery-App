package com.vroomvroom.android.view.ui.main

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
import com.vroomvroom.android.model.MerchantModel
import com.vroomvroom.android.model.Option
import com.vroomvroom.android.model.ProductByCategoryModel
import com.vroomvroom.android.utils.OnProductClickListener
import com.vroomvroom.android.view.adapter.CartAdapter
import com.vroomvroom.android.view.adapter.ProductsByCategoryAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MerchantFragment : Fragment(), OnProductClickListener {

    private val viewModel by activityViewModels<MainViewModel>()
    private val productsByCategoryAdapter by lazy { ProductsByCategoryAdapter(this) }
    private val cartAdapter by lazy { CartAdapter() }
    private var isUserScrolling: Boolean = false
    private val args: MerchantFragmentArgs by navArgs()
    private lateinit var binding: FragmentMerchantBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var isCartCardViewVisible = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMerchantBinding.inflate(inflater)
        linearLayoutManager = binding.byCategoryRv.layoutManager as LinearLayoutManager
        return binding.root
    }
//    .. / .-.. --- ...- . / -.-- --- ..- / .-. --- ... .
//    .- -. / -- .- -.-- / -... .-. --- -. ... .- .-..
    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ctlMerchant.setExpandedTitleColor(ContextCompat.getColor(requireContext(), R.color.white))
        viewModel.queryMerchant(args.id)
        postponeEnterTransition()

        //Tesla soon

        binding.byCategoryRv.adapter = productsByCategoryAdapter

        //private functions
        observeMerchantLiveData()
        observeRoomCartItemLiveData()
        syncTabWithRecyclerView()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnInfo.setOnClickListener {
            viewModel.currentMerchant["merchant"]?.let { merchant ->
                findNavController().navigate(
                    MerchantFragmentDirections.
                    actionMerchantFragmentToMerchantDetailsBottomSheetFragment(merchant))
            }
        }
        binding.merchantRetryButton.setOnClickListener {
            viewModel.queryMerchant(args.id)
            observeMerchantLiveData()
        }
        binding.toggleCart.setOnClickListener {
            findNavController().navigate(R.id.action_merchantFragment_to_cartBottomSheetFragment)
        }
        binding.maximizeCartCardView.setOnClickListener {
            isCartCardViewVisible = true
            toggleCartCardView(isCartCardViewVisible)
        }
        binding.minimizeCartCardView.setOnClickListener {
            isCartCardViewVisible = false
            toggleCartCardView(isCartCardViewVisible)
        }
        cartAdapter.onCartItemClicked = { cartItem ->
            viewModel.updateCartItem(cartItem)
        }
    }

    private fun observeMerchantLiveData() {
        viewModel.merchant.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.merchantConnectionFailedNotice.visibility = View.GONE
                    binding.merchantAppBar.visibility = View.GONE
                    binding.merchantFetchProgress.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    if (response.result.getMerchant == null) {
                        binding.merchantConnectionFailedNotice.visibility = View.VISIBLE
                    } else {
                        binding.merchantConnectionFailedNotice.visibility = View.GONE
                    }
                    startPostponedEnterTransition()
                    val responseMerchant = response.result.getMerchant
                    responseMerchant?.let { merchant ->
                        viewModel.currentMerchant["merchant"] = MerchantModel(
                            merchant.id,
                            merchant.name,
                            merchant.ratingCount,
                            merchant.rating,
                            merchant.location,
                            merchant.opening,
                            merchant.closing
                        )
                        productsByCategoryAdapter.submitList(merchant.products)
                        initializeTabItem(merchant.products)
                        dataBinder(merchant)
                        binding.merchantFetchProgress.visibility = View.GONE
                        binding.merchantAppBar.visibility = View.VISIBLE
                    }
                }
                is ViewState.Error -> {
                    startPostponedEnterTransition()
                    binding.merchantFetchProgress.visibility = View.GONE
                    binding.merchantAppBar.visibility = View.GONE
                    binding.merchantConnectionFailedNotice.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeRoomCartItemLiveData() {
        viewModel.cartItem.observe(viewLifecycleOwner, { items ->
            if (items.isNotEmpty()) {
                var subTotal = 0
                items.forEach { item ->
                    subTotal += item.cartItem.price
                }
                binding.cartFabDetail.text = "Item ${items.size} (₱${subTotal}.00)"
                toggleCartCardView(isCartCardViewVisible)
                binding.maximizeCartCardView.visibility = View.VISIBLE
                cartAdapter.submitList(items)
            } else {
                binding.cartCardVIew.visibility = View.GONE
                binding.maximizeCartCardView.visibility = View.GONE
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun dataBinder(merchant: MerchantQuery.GetMerchant?) {
        val ratingCount = merchant?.ratingCount
        val rating = if (ratingCount!! < 2) {
                "$ratingCount rating"
            } else "$ratingCount ratings"
        binding.ctlMerchant.title = merchant.name
        binding.closingTv.text = "Open until ${merchant.closing} PM"
        binding.merchantRating.text = "${merchant.rating} ($rating)"
        binding.merchantImg.load(merchant.img_url)
    }

    private fun toggleCartCardView(show: Boolean) {
        val transition = Slide(Gravity.END)
        transition.duration = 400
        transition.addTarget(binding.cartCardVIew)

        TransitionManager.beginDelayedTransition(binding.root, transition)
        binding.cartCardVIew.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.maximizeCartCardView.animate().alpha(0f).duration = 300
        } else binding.maximizeCartCardView.animate().alpha(1f).duration = 400
    }

    private fun initializeTabItem(products: List<MerchantQuery.Product?>?) {
        binding.tlMerchant.removeAllTabs()
        products?.forEach { product ->
            binding.tlMerchant.addTab(binding.tlMerchant.newTab().setText(product?.name))
        }
    }


    private fun syncTabWithRecyclerView() {
        // Move recyclerview to the position selected by user
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
        binding.byCategoryRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

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

    override fun onClick(product: MerchantQuery.Product_by_category?) {
        product?.let {
            val options: MutableList<Option> = mutableListOf()
            product.option?.forEach { option ->
                options.add(Option(
                    option!!.name,
                    option.choice
                ))
            }
            val navArgs = ProductByCategoryModel(
                it.id,
                it.name,
                it.product_img_url,
                it.price,
                it.description,
                options
            )
            findNavController().navigate(MerchantFragmentDirections.actionMerchantFragmentToProductBottomSheetFragment(navArgs))
        }
    }
}

@BindingAdapter("bottomSheetProductImageUrl")
fun setBottomSheetProductImageUrl(imageView: ImageView, url: String?) {
    if (url != null) {
        imageView.load(url)
    }
}