package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentMerchantBinding
import com.vroomvroom.android.db.CartItem
import com.vroomvroom.android.db.CartItemChoice
import com.vroomvroom.android.view.adapter.ChoiceAdapter
import com.vroomvroom.android.view.adapter.ProductsByCategoryAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MerchantFragment : Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()
    private val productsByCategoryAdapter by lazy { ProductsByCategoryAdapter() }
    private var isUserScrolling: Boolean = false
    private val args: MerchantFragmentArgs by navArgs()
    private lateinit var binding: FragmentMerchantBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var quantity = 1
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
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cartCardVIew.visibility = View.GONE
        binding.ctlMerchant.setExpandedTitleColor(ContextCompat.getColor(requireContext(), R.color.white))
        viewModel.queryMerchant(args.id)
        postponeEnterTransition()

        //Tesla soon

        binding.byCategoryRv.adapter = productsByCategoryAdapter

        bottomSheetBehavior = BottomSheetBehavior.from(binding.productBottomSheet.root)
        val optionLinearLayout = binding.productBottomSheet.optionLinearLayout
        bottomSheetBehavior.isDraggable = false

        //private functions
        observeMerchantLiveData()
        observeRoomCartItemLiveData()
        syncTabWithRecyclerView(bottomSheetBehavior)

        binding.productBottomSheet.minimizeBottomSheet.setOnClickListener {
            collapseBottomSheet(bottomSheetBehavior)
        }
        productsByCategoryAdapter.product.observe(viewLifecycleOwner, { product ->
            if (product != null) {
                product.option?.forEach { option ->
                    initializeOptionView(option, optionLinearLayout)
                }
                binding.cartCardVIew.visibility = View.GONE
                binding.merchantAppBar.animate().alpha(0.1f).duration = 200
                binding.viewBg.animate().alpha(0.8f).duration = 200
                binding.productBottomSheet.product = product
                if (product.description.isNullOrBlank()) {
                    binding.productBottomSheet.bottomSheetTextDescription.visibility = View.GONE
                } else binding.productBottomSheet.bottomSheetTextDescription.visibility = View.VISIBLE
                if (product.product_img_url.isNullOrBlank()) {
                    binding.productBottomSheet.bottomSheetCardView.visibility = View.GONE
                } else binding.productBottomSheet.bottomSheetCardView.visibility = View.VISIBLE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                binding.productBottomSheet.btnAddToCart.setOnClickListener {
                    var choicePrice = 0
                    viewModel.optionMap.forEach { (_, value) ->
                        if (value.additional_price != null)
                            choicePrice += value.additional_price
                    }
                    val cartItem = CartItem(
                        remote_id = product.id,
                        merchant = viewModel.currentMerchant.toString(),
                        name = product.name,
                        product_img_url = product.product_img_url,
                        price = (product.price + choicePrice) * quantity
                    )
                    viewModel.insertCartItem(cartItem)
                    collapseBottomSheet(bottomSheetBehavior)
                }
            }
        })
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    optionLinearLayout.removeAllViews()
                    binding.btnBack.isClickable = true
                    binding.btnInfo.isClickable = true
                    binding.viewBg.isClickable = false
                    binding.merchantAppBar.animate().alpha(1f).duration = 200
                    binding.viewBg.animate().alpha(0f).duration = 200
                    binding.productBottomSheet.quantity.text = "1"
                    quantity = 1
                    observeRoomCartItemLiveData()
                    requireActivity().onBackPressedDispatcher.addCallback(
                        viewLifecycleOwner,
                        onBackPressedNavigationCallback
                    )
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    toggleCartCardView(false)
                    binding.merchantAppBar.setExpanded(false, true)
                    binding.merchantAppBar.animate().alpha(0.1f).duration = 0
                    binding.viewBg.animate().alpha(0.8f).duration = 200
                    binding.btnBack.isClickable = false
                    binding.btnInfo.isClickable = false
                    binding.tlMerchant.isEnabled = false
                    binding.toolbarMerchant.setOnClickListener {
                        collapseBottomSheet(bottomSheetBehavior)
                    }
                    binding.viewBg.isClickable = true
                    binding.viewBg.setOnClickListener {
                        collapseBottomSheet(bottomSheetBehavior)
                    }
                    requireActivity().onBackPressedDispatcher.addCallback(
                        viewLifecycleOwner,
                        onBackPressedBottomSheetCallback
                    )
                }
                BottomSheetBehavior.STATE_DRAGGING -> {}
            }
        }
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}

    })
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.merchantRetryButton.setOnClickListener {
            viewModel.queryMerchant(args.id)
            observeMerchantLiveData()
        }
        binding.maximizeCartCardView.setOnClickListener {
            isCartCardViewVisible = true
            toggleCartCardView(isCartCardViewVisible)
        }
        binding.minimizeCartCardView.setOnClickListener {
            isCartCardViewVisible = false
            toggleCartCardView(isCartCardViewVisible)
        }

        binding.productBottomSheet.decreaseQuantity.setOnClickListener {
            if (quantity != 1) {
                quantity -= 1
                binding.productBottomSheet.quantity.text = quantity.toString()
            }
        }

        binding.productBottomSheet.increaseQuantity.setOnClickListener {
            quantity += 1
            binding.productBottomSheet.quantity.text = quantity.toString()
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
                    if (response.value?.data == null) {
                        binding.merchantConnectionFailedNotice.visibility = View.VISIBLE
                    } else {
                        binding.merchantConnectionFailedNotice.visibility = View.GONE
                    }
                    startPostponedEnterTransition()
                    val merchant = response.value?.data?.getMerchant
                    viewModel.currentMerchant = merchant?.name
                    productsByCategoryAdapter.submitList(merchant?.products)
                    initializeTabItem(merchant?.products)
                    viewBinder(merchant)
                    binding.merchantFetchProgress.visibility = View.GONE
                    binding.merchantAppBar.visibility = View.VISIBLE
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

    private val onBackPressedBottomSheetCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                collapseBottomSheet(bottomSheetBehavior)
            }
        }

    private val onBackPressedNavigationCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }

    @SuppressLint("SetTextI18n")
    private fun observeRoomCartItemLiveData() {
        viewModel.cartItem.observe(viewLifecycleOwner, { items ->
            if (items.isNotEmpty()) {
                var totalCartValue = 0
                items.forEach { item ->
                    totalCartValue += item.cartItem.price
                }
                binding.cartFabDetail.text = "Item ${items.size} (₱${totalCartValue}.00)"
                toggleCartCardView(isCartCardViewVisible)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun viewBinder(merchant: MerchantQuery.GetMerchant?) {
        val ratingCount = merchant?.ratingCount
        val rating = if (ratingCount!! < 2) {
                "$ratingCount rating"
            } else "$ratingCount ratings"
        binding.ctlMerchant.title = merchant.name
        binding.closingTv.text = "Open until ${merchant.closing} PM"
        binding.merchantRating.text = "${merchant.rating} ($rating)"
        binding.merchantImg.load(merchant.img_url)
    }

    private fun initializeTabItem(products: List<MerchantQuery.Product?>?) {
        products?.forEach { product ->
            binding.tlMerchant.addTab(binding.tlMerchant.newTab().setText(product?.name))
        }
    }

    private fun toggleCartCardView(show: Boolean) {
        val transition = Slide(Gravity.RIGHT)
        transition.duration = 600
        transition.addTarget(binding.cartCardVIew)

        TransitionManager.beginDelayedTransition(binding.root, transition)
        binding.cartCardVIew.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.maximizeCartCardView.animate().alpha(0f).duration = 300
        } else binding.maximizeCartCardView.animate().alpha(1f).duration = 600
    }

    private fun initializeOptionView(option: MerchantQuery.Option?, optionLinearLayout: LinearLayout) {
        val titleTv = TextView(requireContext())
        val choiceRv = RecyclerView(requireContext())
        val choiceAdapter = ChoiceAdapter(option!!.choice)
        choiceAdapter.optionType = option.name
        titleTv.text = option.name
        titleTv.textSize = 16f
        choiceRv.layoutManager = LinearLayoutManager(requireContext())
        choiceRv.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        choiceRv.isNestedScrollingEnabled = false
        choiceRv.adapter = choiceAdapter
        optionLinearLayout.addView(titleTv)
        optionLinearLayout.addView(choiceRv)
        choiceAdapter.onChoiceClicked = { choice ->
            val optionType = choiceAdapter.optionType.toString()
            viewModel.optionMap[optionType] =
                CartItemChoice(
                    name = choice.name,
                    additional_price = choice.additional_price,
                    optionType = optionType
                )
        }
    }

    private fun syncTabWithRecyclerView(bottomSheetBehavior: BottomSheetBehavior<View>) {
        // Move recyclerview to the position selected by user
        binding.tlMerchant.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                collapseBottomSheet(bottomSheetBehavior)
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

    private fun collapseBottomSheet(bottomSheetBehavior: BottomSheetBehavior<View>) {
        binding.merchantAppBar.animate().alpha(1f).duration = 200
        binding.viewBg.animate().alpha(0f).duration = 200
        binding.productBottomSheet.product = null
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        binding.bottomSheetCoordinator.background = null
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