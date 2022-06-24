package com.vroomvroom.android.view.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.R
import com.vroomvroom.android.view.ui.account.viewmodel.AccountViewModel
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import com.vroomvroom.android.view.ui.home.viewmodel.HomeViewModel
import com.vroomvroom.android.view.ui.location.viewmodel.LocationViewModel
import com.vroomvroom.android.view.ui.orders.viewmodel.OrdersViewModel
import com.vroomvroom.android.view.ui.common.CommonAlertDialog
import com.vroomvroom.android.view.ui.common.LoadingDialog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.IllegalArgumentException

@ExperimentalCoroutinesApi
abstract class BaseBottomSheetFragment<VB: ViewBinding> (
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : BottomSheetDialogFragment() {

    val mainActivityViewModel by activityViewModels<MainViewModel>()

    val locationViewModel by viewModels<LocationViewModel>()
    val authViewModel by viewModels<AuthViewModel>()
    val accountViewModel by viewModels<AccountViewModel>()
    val ordersViewModel by viewModels<OrdersViewModel>()
    val homeViewModel by viewModels<HomeViewModel>()

    val dialog by lazy { CommonAlertDialog(requireActivity()) }
    val loadingDialog by lazy { LoadingDialog(requireActivity()) }

    private var _binding: VB? = null
    val binding: VB
        get() = _binding as VB

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater.invoke(inflater)
        if (_binding == null)
            throw IllegalArgumentException("Binding cannot be null")
        binding.root.setBackgroundResource(R.drawable.bg_white_fff_rounded_24dp_top)
        return binding.root
    }

    fun showShortToast(message: Int) {
        Toast.makeText(requireContext(), getString(message), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int {
        return R.style.NoBackgroundDialogTheme
    }

}