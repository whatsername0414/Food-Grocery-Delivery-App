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
import com.vroomvroom.android.databinding.FragmentProductBottomSheetBinding
import com.vroomvroom.android.db.CartItem
import com.vroomvroom.android.db.CartItemChoice
import com.vroomvroom.android.model.Option
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

        val product = navArgs.productByCategory
        binding.product = product
        navArgs.productByCategory.option?.forEach { option ->
            initializeOptionView(option)
        }

        if (navArgs.productByCategory.description.isNullOrBlank()) {
            binding.bottomSheetTextDescription.visibility = View.GONE
        } else binding.bottomSheetTextDescription.visibility = View.VISIBLE
        if (navArgs.productByCategory.product_img_url.isNullOrBlank()) {
            binding.bottomSheetCardView.visibility = View.GONE
        } else binding.bottomSheetCardView.visibility = View.VISIBLE

        binding.btnAddToCart.setOnClickListener {
            var choicePrice = 0
            viewModel.optionMap.forEach { (_, value) ->
                if (value.additional_price != null)
                    choicePrice += value.additional_price
            }
            val cartItem = CartItem(
                remote_id = product.id,
                merchant_id = viewModel.currentMerchant["merchant"]!!.id,
                merchant = viewModel.currentMerchant["merchant"]!!.name,
                name = product.name,
                product_img_url = product.product_img_url,
                price = (product.price + choicePrice) * quantity,
                quantity = quantity,
                special_instructions =
                if (binding.bottomSheetTextDescription.text.isNotEmpty())
                    binding.bottomSheetTextDescription.text.toString()
                else null
            )
            viewModel.insertCartItem(cartItem)
            findNavController().popBackStack()
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

    private fun initializeOptionView(option: Option?) {
        val titleTv = TextView(requireContext())
        val choiceRv = RecyclerView(requireContext())
        option?.let { nullSafeOption ->
            val choiceAdapter = ChoiceAdapter(nullSafeOption.choice)
            choiceAdapter.optionType = nullSafeOption.name
            titleTv.text = nullSafeOption.name
            titleTv.textSize = 16f
            choiceRv.layoutManager = LinearLayoutManager(requireContext())
            choiceRv.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
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
    }
}