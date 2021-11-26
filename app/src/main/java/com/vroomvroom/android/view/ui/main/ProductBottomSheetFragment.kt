package com.vroomvroom.android.view.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentProductBottomSheetBinding
import com.vroomvroom.android.domain.db.CartItemEntity
import com.vroomvroom.android.domain.db.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.MerchantEntity
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.domain.model.product.Option
import com.vroomvroom.android.domain.model.product.Product
import com.vroomvroom.android.view.adapter.ChoiceAdapter
import com.vroomvroom.android.utils.Utils.clearFocus
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProductBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentProductBottomSheetBinding
    private lateinit var optionLinearLayout: LinearLayout

    private val navArgs: ProductBottomSheetFragmentArgs by navArgs()
    private val viewModel by activityViewModels<MainViewModel>()

    private var quantity = 1

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = navArgs.product
        val currentMerchant = viewModel.currentMerchant["merchant"]!!
        binding.product = product
        navArgs.product.option?.forEach { option ->
            initializeProductOption(option)
        }

        if (product.description.isNullOrBlank()) {
            binding.bottomSheetTextDescription.visibility = View.GONE
        } else binding.bottomSheetTextDescription.visibility = View.VISIBLE
        if (product.product_img_url.isNullOrBlank()) {
            binding.bottomSheetCardView.visibility = View.GONE
        } else binding.bottomSheetCardView.visibility = View.VISIBLE

        viewModel.cartItem.observe(viewLifecycleOwner, { items ->
            val isCartNotEmpty = items.isNotEmpty()
            binding.btnAddToCart.setOnClickListener {
                if (isCartNotEmpty && items.first().cartItemEntity.merchant.merchant_name != currentMerchant.name) {
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
                viewModel.deleteAllCartItemChoice()
            }
            .show()
    }

    private fun cartItemBuilder(
        product: Product,
        currentMerchant: Merchant
    ): CartItemEntity {
        var choicePrice = 0
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
            remote_id = product.id,
            merchant = merchant,
            name = product.name,
            product_img_url = product.product_img_url,
            price = (product.price + choicePrice) * quantity,
            quantity = quantity,
            special_instructions =
            if (binding.instructionInputEditText.text.isNullOrBlank()) null
            else binding.instructionInputEditText.text.toString()
        )
    }

    private fun initializeProductOption(option: Option?) {
        val titleTv = TextView(requireContext())
        val choiceRv = RecyclerView(requireContext())
        option?.let {
            val choiceAdapter = ChoiceAdapter(it.choice)
            choiceAdapter.optionType = it.name
            titleTv.text = it.name
            titleTv.textSize = 16f
            choiceRv.layoutManager = LinearLayoutManager(requireContext())
            choiceRv.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            choiceRv.adapter = choiceAdapter
            optionLinearLayout.addView(titleTv)
            optionLinearLayout.addView(choiceRv)
            choiceAdapter.onChoiceClicked = { choice ->
                val optionType = choiceAdapter.optionType.toString()
                viewModel.optionMap[optionType] =
                    CartItemChoiceEntity(
                        name = choice.name,
                        additional_price = choice.additional_price,
                        optionType = optionType
                    )
            }
        }
    }
}