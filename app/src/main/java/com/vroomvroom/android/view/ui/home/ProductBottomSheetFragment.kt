package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentProductBottomSheetBinding
import com.vroomvroom.android.domain.db.cart.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.cart.CartItemEntity
import com.vroomvroom.android.domain.db.cart.CartMerchantEntity
import com.vroomvroom.android.domain.model.product.Option
import com.vroomvroom.android.domain.model.product.Product
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Utils.clearFocus
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import com.vroomvroom.android.view.ui.home.adapter.ChoiceAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProductBottomSheetFragment : BaseBottomSheetFragment<FragmentProductBottomSheetBinding>(
    FragmentProductBottomSheetBinding::inflate
) {

    private lateinit var optionLinearLayout: LinearLayout
    private val navArgs: ProductBottomSheetFragmentArgs by navArgs()
    private var quantity = 1
    private var required: Int? = null

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        optionLinearLayout = binding.optionLinearLayout
        clearFocus(
            binding.root,
            binding.instructionInputEditText,
            requireActivity()
        )

        val product = navArgs.product
        Glide
            .with(this)
            .load(product.product_img_url)
            .placeholder(R.drawable.ic_placeholder)
            .into(binding.bottomSheetProductImg)

        val currentMerchant = mainActivityViewModel.merchant
        binding.productPrice.text = "₱${"%.2f".format(product.price)}"
        binding.product = product
        product.option?.forEach { option ->
            initializeProductOption(option)
        }
        required = product.option?.filter { it.required }?.size
        checkRequired()
        binding.bottomSheetTextDescription.isVisible = !product.description.isNullOrBlank()

        homeViewModel.cartItem.observe(viewLifecycleOwner) { items ->
            binding.btnAddToCart.setOnClickListener {
                if (items.isNotEmpty() && items.first().cartItemEntity.cartMerchant.merchantName != currentMerchant.name) {
                    dialog.show(
                        getString(R.string.prompt),
                        getString(R.string.clear_cart_message),
                        getString(R.string.cancel),
                        getString(R.string.remove)
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                homeViewModel.deleteAllCartItem()
                                homeViewModel.insertCartItem(cartItemBuilder(product, currentMerchant))
                                dialog.dismiss()
                                findNavController().popBackStack()
                            }
                            ClickType.NEGATIVE -> dialog.dismiss()
                        }
                    }
                } else {
                    homeViewModel.insertCartItem(cartItemBuilder(product, currentMerchant))
                    findNavController().popBackStack()
                }
            }
        }

        binding.decreaseQuantity.setOnClickListener {
            if (quantity != 1) {
                quantity -= 1
                binding.quantity.text = quantity.toString()
            }
        }

        binding.increaseQuantity.setOnClickListener {
            quantity += 1
            binding.quantity.text = quantity.toString()
        }

    }

    private fun cartItemBuilder(
        product: Product,
        currentMerchant: MerchantQuery.GetMerchant
    ): CartItemEntity {
        var choicePrice = 0.0
        homeViewModel.optionMap.forEach { (_, value) ->
            value.additionalPrice?.let {
                choicePrice += it
            }
        }
        val merchant = CartMerchantEntity(
            merchantId = currentMerchant._id,
            merchantName = currentMerchant.name
        )
        return CartItemEntity(
            cartMerchant = merchant,
            productId = product.id,
            name = product.name,
            productImgUrl = product.product_img_url,
            price = (product.price + choicePrice) * quantity,
            quantity = quantity,
            specialInstructions =
            if (binding.instructionInputEditText.text.isNullOrBlank()) null
            else binding.instructionInputEditText.text.toString()
        )
    }

    @SuppressLint("SetTextI18n")
    private fun initializeProductOption(option: Option) {
        val titleTv = TextView(requireContext())
        val requiredTv = TextView(requireContext())
        val choiceRv = RecyclerView(requireContext())
        val choiceAdapter = ChoiceAdapter(option.choice)
        titleTv.apply {
            text = "${option.name} :"
            textSize = 16f
        }
        requiredTv.apply {
            text = "*required"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.red_a30))
            visibility = if (option.required) View.VISIBLE else View.GONE
        }
        choiceRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            adapter = choiceAdapter
        }
        optionLinearLayout.apply {
            addView(titleTv)
            addView(requiredTv)
            addView(choiceRv)
        }
        choiceAdapter.apply {
            optionType = option.name
            onChoiceClicked = { choice ->
                val optionType = optionType.toString()
                homeViewModel.optionMap[optionType] =
                    CartItemChoiceEntity(
                        name = choice.name,
                        additionalPrice = choice.additional_price,
                        optionType = optionType
                    )
                checkRequired()
            }
        }
    }

    private fun checkRequired() {
        required?.let {
            if (homeViewModel.optionMap.size < it) {
                binding.btnAddToCart.apply {
                    isEnabled = false
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray_a5a))
                }
            } else {
                binding.btnAddToCart.apply {
                    isEnabled = true
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red_a30))
                }
            }
        }
    }
}