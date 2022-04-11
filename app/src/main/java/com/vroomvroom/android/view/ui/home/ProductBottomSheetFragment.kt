package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentProductBottomSheetBinding
import com.vroomvroom.android.domain.db.cart.CartItemEntity
import com.vroomvroom.android.domain.db.cart.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.cart.MerchantEntity
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.domain.model.product.Option
import com.vroomvroom.android.domain.model.product.Product
import com.vroomvroom.android.view.ui.home.adapter.ChoiceAdapter
import com.vroomvroom.android.utils.Utils.clearFocus
import com.vroomvroom.android.view.ui.home.viewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProductBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentProductBottomSheetBinding
    private lateinit var optionLinearLayout: LinearLayout

    private val navArgs: ProductBottomSheetFragmentArgs by navArgs()
    private val viewModel by viewModels<HomeViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private var quantity = 1
    private var required: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductBottomSheetBinding.inflate(inflater)
        optionLinearLayout = binding.optionLinearLayout
        clearFocus(
            binding.root,
            binding.instructionInputEditText,
            requireActivity()
        )
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = navArgs.product
        val currentMerchant = activityViewModel.currentMerchant["merchant"]!!
        binding.productPrice.text = "₱${"%.2f".format(product.price)}"
        binding.product = product
        product.option?.forEach { option ->
            initializeProductOption(option)
        }
        required = product.option?.filter { it.required }?.size
        checkRequired()
        if (product.description.isNullOrBlank()) {
            binding.bottomSheetTextDescription.visibility = View.GONE
        } else binding.bottomSheetTextDescription.visibility = View.VISIBLE
        if (product.product_img_url.isNullOrBlank()) {
            binding.bottomSheetCardView.visibility = View.GONE
        } else binding.bottomSheetCardView.visibility = View.VISIBLE

        viewModel.cartItem.observe(viewLifecycleOwner, { items ->
            binding.btnAddToCart.setOnClickListener {
                if (items.isNotEmpty() && items.first().cartItemEntity.merchant.merchant_name != currentMerchant.name) {
                    showDialog()
                } else {
                    viewModel.insertCartItem(cartItemBuilder(product, currentMerchant))
                    findNavController().popBackStack()
                }
            }
        })

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

    private fun showDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.clear_cart))
            .setMessage(getString(R.string.clear_cart_message))
            .setNeutralButton(getString(R.string.maps_alert_dialog_cancel)) { _, _ ->
                findNavController().popBackStack()
            }
            .setPositiveButton(getString(R.string.proceed)) { _, _ ->
                viewModel.deleteAllCartItem()
            }
            .show()
    }

    private fun cartItemBuilder(
        product: Product,
        currentMerchant: Merchant
    ): CartItemEntity {
        var choicePrice = 0.0
        viewModel.optionMap.forEach { (_, value) ->
            value.additional_price?.let {
                choicePrice += it
            }
        }
        val merchant = MerchantEntity(
            merchant_id = currentMerchant.id,
            merchant_name = currentMerchant.name
        )
        return CartItemEntity(
            merchant = merchant,
            product_id = product.id,
            name = product.name,
            product_img_url = product.product_img_url,
            price = (product.price + choicePrice) * quantity,
            quantity = quantity,
            special_instructions =
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
            setTextColor(ContextCompat.getColor(requireContext(), R.color.maroon))
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
                viewModel.optionMap[optionType] =
                    CartItemChoiceEntity(
                        name = choice.name,
                        additional_price = choice.additional_price,
                        optionType = optionType
                    )
                checkRequired()
            }
        }
    }

    private fun checkRequired() {
        required?.let {
            if (viewModel.optionMap.size < it) {
                binding.btnAddToCart.apply {
                    isEnabled = false
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray))
                }
            } else {
                binding.btnAddToCart.apply {
                    isEnabled = true
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.maroon))
                }
            }
        }
    }
}