package com.vroomvroom.android.view.ui.base

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.CommonNoticeLayoutBinding
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.account.viewmodel.AccountViewModel
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import com.vroomvroom.android.view.ui.browse.viewmodel.BrowseViewModel
import com.vroomvroom.android.view.ui.home.adapter.MerchantAdapter
import com.vroomvroom.android.view.ui.home.viewmodel.HomeViewModel
import com.vroomvroom.android.view.ui.location.viewmodel.LocationViewModel
import com.vroomvroom.android.view.ui.orders.viewmodel.OrdersViewModel
import com.vroomvroom.android.view.ui.widget.LoadingDialog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.IllegalArgumentException

@ExperimentalCoroutinesApi
abstract class BaseFragment<VB: ViewBinding> (
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : Fragment() {

    val mainActivityViewModel by activityViewModels<MainViewModel>()
    val authViewModel by activityViewModels<AuthViewModel>()

    val mainViewModel by viewModels<MainViewModel>()
    val locationViewModel by viewModels<LocationViewModel>()
    val accountViewModel by viewModels<AccountViewModel>()
    val ordersViewModel by viewModels<OrdersViewModel>()
    val homeViewModel by viewModels<HomeViewModel>()
    val browseViewModel by viewModels<BrowseViewModel>()

    val loadingDialog by lazy { LoadingDialog(requireActivity()) }

    private var _binding: VB? = null
    val binding: VB
        get() = _binding as VB

    lateinit var navController: NavController
    private lateinit var snackBar: Snackbar


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater.invoke(inflater)
        if (_binding == null)
            throw IllegalArgumentException("Binding cannot be null")
        return binding.root
    }

    fun observeFavorite(
        merchantAdapter: MerchantAdapter,
        merchant: Merchant,
        position: Int,
        direction: Int
    ) {
        homeViewModel.favorite.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> Unit
                is ViewState.Success -> {
                    if (direction == Constants.ADD_TO_FAVORITES) {
                        showShortSnackBar("Added to favorites")
                        merchant.favorite = true
                        merchantAdapter.notifyItemChanged(position, merchant)
                    } else {
                        showShortSnackBar("Removed from favorites")
                        merchant.favorite = false
                        merchantAdapter.notifyItemChanged(position, merchant)
                    }
                    homeViewModel.favorite.removeObservers(viewLifecycleOwner)
                }
                is ViewState.Error -> {
                    showShortToast(R.string.general_error_message)
                    merchantAdapter.notifyItemChanged(position)
                    homeViewModel.favorite.removeObservers(viewLifecycleOwner)
                }
            }
        }
    }

    fun Toolbar.setupToolbar() {
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        this.setupWithNavController(navController, appBarConfiguration)
        this.setNavigationIcon(R.drawable.ic_arrow_left)
    }

    fun showShortToast(message: Int) {
        Toast.makeText(requireContext(), getString(message), Toast.LENGTH_SHORT).show()
    }

    fun showShortToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun CommonNoticeLayoutBinding.hideNotice() {
        this.root.visibility = View.GONE
    }

    fun CommonNoticeLayoutBinding.showNotice(
        image: Int,
        title: Int,
        message: Int,
        messageParam: String?,
        btnTitle: Int?,
        isButtonVisible: Boolean = true,
        listener: () -> Unit
    ) {
        this.apply {
            this.root.visibility = View.VISIBLE
            noticeImage.setImageResource(image)
            noticeTitle.text = getString(title)
            noticeMessage.text = Html.fromHtml(getString(message, messageParam), FROM_HTML_MODE_LEGACY)
            if (isButtonVisible) {
                btnNotice.apply {
                    text = getString(btnTitle ?: -1)
                    setOnClickListener {
                        listener.invoke()
                    }
                }
            } else {
                btnNotice.visibility = View.GONE
            }

        }
    }

    fun CommonNoticeLayoutBinding.showNetworkError(listener: () -> Unit) {
        showNotice(
            R.drawable.ic_no_wifi,
            R.string.network_error,
            R.string.network_error_message,
            null,
            R.string.retry,
            true,
            listener
        )
    }

    fun CommonNoticeLayoutBinding.showEmptyOrder(listener: () -> Unit) {
        showNotice(
            R.drawable.ic_invoice,
            R.string.empty_order,
            R.string.empty_order_message,
            null,
            R.string.start_shopping,
            true,
            listener
        )
    }

    private fun showShortSnackBar(message: String) {
        snackBar = Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        )
        if (mainActivityViewModel.isBottomBarVisible) {
            val snackBarView = snackBar.view
            snackBarView.translationY = - (400 / requireContext().resources.displayMetrics.density)
        }
        snackBar.show()
    }
}