package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCartBottomSheetBinding
import com.vroomvroom.android.domain.db.CartItemWithChoice
import com.vroomvroom.android.view.adapter.CartAdapter
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CartBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCartBottomSheetBinding
    private val cartAdapter by lazy { CartAdapter() }
    private val viewModel by activityViewModels<MainViewModel>()
    private var currentMerchantId: String? = null

        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cartItemRv.adapter = cartAdapter

        observeRoomCartItem()

        cartAdapter.onCartItemClicked = { cartItem ->
            viewModel.updateCartItem(cartItem)
        }

        cartAdapter.onDeleteCartItemClick = { cartItemWithChoice ->
            deleteCartItemWithChoice(cartItemWithChoice)
        }

        binding.btnStartShopping.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddItems.setOnClickListener {
            val previousDestination = findNavController().previousBackStackEntry?.destination?.id
            if (currentMerchantId.isNullOrBlank()) {
                findNavController().popBackStack()
            } else {
                currentMerchantId?.let { id ->
                    if (previousDestination == R.id.homeFragment) {
                        findNavController().navigate(
                            CartBottomSheetFragmentDirections
                                .actionCartBottomSheetFragmentToMerchantFragment(id))
                    } else findNavController().popBackStack()
                }
            }
        }

        binding.btnCheckOut.setOnClickListener {
            findNavController().navigate(R.id.action_cartBottomSheetFragment_to_checkoutFragment)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeRoomCartItem() {
        viewModel.cartItem.observe(viewLifecycleOwner, { items ->
            var subTotal = 0
            if (items.isEmpty()) {
                cartAdapter.submitList(emptyList())
                binding.cartLayout.visibility = View.GONE
                binding.emptyCartLayout.visibility = View.VISIBLE
                currentMerchantId = null
            } else {
                cartAdapter.submitList(items)
                items.forEach { item ->
                    subTotal += item.cartItemEntity.price
                }
                currentMerchantId = items.first().cartItemEntity.merchant.merchant_id
                binding.merchantName.text = items.first().cartItemEntity.merchant.merchant_name
                binding.subtotalTv.text = "₱$subTotal.00"
                binding.cartLayout.visibility = View.VISIBLE
                binding.emptyCartLayout.visibility = View.GONE
            }
        })
    }

    private fun deleteCartItemWithChoice(cartItemWithChoice: CartItemWithChoice) {
        AlertDialog.Builder(requireContext())
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteCartItem(cartItemWithChoice.cartItemEntity)
                cartItemWithChoice.choiceEntities?.forEach { cartItemChoice ->
                    viewModel.deleteCartItemChoice(cartItemChoice)
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to remove " +
                "${cartItemWithChoice.cartItemEntity.name} from the cart?")
            .create().show()
    }
}