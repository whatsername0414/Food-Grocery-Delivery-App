package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCartBottomSheetBinding
import com.vroomvroom.android.data.model.cart.CartItemWithOptions
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.utils.Utils.timeFormatter
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import com.vroomvroom.android.view.ui.home.adapter.CartAdapter
import com.vroomvroom.android.view.ui.common.CommonAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CartBottomSheetFragment : BaseBottomSheetFragment<FragmentCartBottomSheetBinding>(
    FragmentCartBottomSheetBinding::inflate
) {

    private val cartAdapter by lazy { CartAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cartItemRv.adapter = cartAdapter

        observeRoomCartItem()
        observeMerchant()

        cartAdapter.onCartItemClicked = { cartItem ->
            homeViewModel.updateCartItem(cartItem)
        }

        cartAdapter.onDeleteCartItemClick = { cartItemWithChoice ->
            initAlertDialog(cartItemWithChoice)
        }

        binding.btnStartShopping.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddItems.setOnClickListener {
            val previousDestination = findNavController().previousBackStackEntry?.destination?.id
            if (previousDestination == R.id.homeFragment) {
                findNavController().navigate(
                    CartBottomSheetFragmentDirections
                        .actionCartBottomSheetFragmentToMerchantFragment(homeViewModel.currentMerchantId))
            } else findNavController().popBackStack()
        }

        binding.btnCheckOut.setOnClickListener {
            homeViewModel.getMerchant(homeViewModel.currentMerchantId)
        }
    }

    private fun observeMerchant() {
        homeViewModel.merchant.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    binding.cartProgress.visibility = View.VISIBLE
                    binding.btnCheckOut.isEnabled = false
                }
                is Resource.Success -> {
                    binding.cartProgress.visibility = View.GONE
                    binding.btnCheckOut.isEnabled = true
                    val merchant = response.data
                    if (merchant.isOpen) {
                        findNavController().safeNavigate(
                            CartBottomSheetFragmentDirections
                                .actionCartBottomSheetFragmentToCheckoutFragment())
                    } else {
                        dialog.show(
                            getString(R.string.closed),
                            getString(R.string.closed_alert_label, timeFormatter(merchant.opening)),
                            getString(R.string.cancel),
                            getString(R.string.okay),
                            false
                        ) { type ->
                            when (type) {
                                ClickType.POSITIVE -> dialog.dismiss()
                                ClickType.NEGATIVE -> Unit
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    binding.cartProgress.visibility = View.GONE
                    binding.btnCheckOut.isEnabled = true
                    dialog.show(
                        getString(R.string.network_error),
                        getString(R.string.network_error_message),
                        getString(R.string.cancel),
                        getString(R.string.retry),
                        false
                    ) { type ->
                        when (type) {
                            ClickType.POSITIVE -> {
                                homeViewModel.getMerchant(homeViewModel.currentMerchantId)
                                dialog.dismiss()
                            }
                            ClickType.NEGATIVE -> dialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeRoomCartItem() {
        homeViewModel.cartItem.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                cartAdapter.submitList(emptyList())
                binding.cartLayout.visibility = View.GONE
                binding.emptyCartLayout.visibility = View.VISIBLE
            } else {
                cartAdapter.submitList(items)
                val subTotal = items.sumOf { it.cartItem.price }
                homeViewModel.currentMerchantId = items.first().cartItem.cartMerchant.merchantId
                binding.merchantName.text = items.first().cartItem.cartMerchant.merchantName
                binding.subtotalTv.text = "₱${"%.2f".format(subTotal)}"
                binding.cartLayout.visibility = View.VISIBLE
                binding.emptyCartLayout.visibility = View.GONE
            }
        }
    }

    private fun initAlertDialog(cartItemWithOptions: CartItemWithOptions) {
        val dialog = CommonAlertDialog(
            requireActivity()
        )
        dialog.show(
            getString(R.string.cart_delete_title),
            getString(R.string.cart_delete_message, cartItemWithOptions.cartItem.name),
            getString(R.string.cancel),
            getString(R.string.cart_delete_button)
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    homeViewModel.deleteCartItem(cartItemWithOptions.cartItem)
                    cartItemWithOptions.cartItemOptions?.let {
                        homeViewModel.deleteAllCartItemChoice()
                    }
                    dialog.dismiss()
                }
                ClickType.NEGATIVE -> dialog.dismiss()
            }
        }
    }
}