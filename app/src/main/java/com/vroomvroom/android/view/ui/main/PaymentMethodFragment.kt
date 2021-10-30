package com.vroomvroom.android.view.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentPaymentMethodBinding
import com.vroomvroom.android.viewmodel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class PaymentMethodFragment : Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var binding: FragmentPaymentMethodBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentMethodBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.paymentMethodToolbar.setupWithNavController(navController, appBarConfiguration)

        binding.cashConstraint.setOnClickListener {
            viewModel.paymentMethod.postValue("Cash")
            navController.popBackStack()
        }

        binding.gcashConstraint.setOnClickListener {
            viewModel.paymentMethod.postValue("GCash")
            navController.popBackStack()
        }
    }
}