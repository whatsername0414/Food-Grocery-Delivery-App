package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCartBottomSheetBinding
import com.vroomvroom.android.domain.db.cart.CartItemWithChoice
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.utils.Utils.timeFormatter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import com.vroomvroom.android.view.ui.home.adapter.CartAdapter
import com.vroomvroom.android.view.ui.widget.CommonAlertDialog
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
            homeViewModel.queryMerchant(homeViewModel.currentMerchantId)
        }
    }

    private fun observeMerchant() {
        homeViewModel.merchant.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.cartProgress.visibility = View.VISIBLE
                    binding.btnCheckOut.isEnabled = false
                }
                is ViewState.Success -> {
                    binding.cartProgress.visibility = View.GONE
                    binding.btnCheckOut.isEnabled = true
                    val merchant = response.result.getMerchant
                    if (merchant.isOpen) {
                        findNavController().safeNavigate(
                            CartBottomSheetFragmentDirections
                                .actionCartBottomSheetFragmentToCheckoutFragment())
                    } else {
                        dialog.show(
                            getString(R.string.prompt),
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
                is ViewState.Error -> {
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
                                homeViewModel.queryMerchant(homeViewModel.currentMerchantId)
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
            var subTotal = 0.0
            if (items.isEmpty()) {
                cartAdapter.submitList(emptyList())
                binding.cartLayout.visibility = View.GONE
                binding.emptyCartLayout.visibility = View.VISIBLE
            } else {
                cartAdapter.submitList(items)
                items.forEach { item ->
                    subTotal += item.cartItemEntity.price
                }
                homeViewModel.currentMerchantId = items.first().cartItemEntity.cartMerchant.merchantId
                binding.merchantName.text = items.first().cartItemEntity.cartMerchant.merchantName
                binding.subtotalTv.text = "₱${"%.2f".format(subTotal)}"
                binding.cartLayout.visibility = View.VISIBLE
                binding.emptyCartLayout.visibility = View.GONE
            }
        }
    }

    private fun initAlertDialog(cartItemWithChoice: CartItemWithChoice) {
        val dialog = CommonAlertDialog(
            requireActivity()
        )
        dialog.show(
            getString(R.string.cart_delete_title),
            getString(R.string.cart_delete_message, cartItemWithChoice.cartItemEntity.name),
            getString(R.string.cancel),
            getString(R.string.cart_delete_button)
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    homeViewModel.deleteCartItem(cartItemWithChoice.cartItemEntity)
                    cartItemWithChoice.choiceEntities?.let {
                        homeViewModel.deleteAllCartItemChoice()
                    }
                    dialog.dismiss()
                }
                ClickType.NEGATIVE -> dialog.dismiss()
            }
        }
    }
}