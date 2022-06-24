package com.vroomvroom.android.view.ui.common

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCommonCompleteBinding
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.view.ui.base.BaseFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class CommonCompleteFragment : BaseFragment<FragmentCommonCompleteBinding>(
    FragmentCommonCompleteBinding::inflate
) {

    private val args: CommonCompleteFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        binding.appBarLayout.toolbar.apply {
            setupToolbar()
            navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close_maroon)
            setNavigationOnClickListener {
                navController.safeNavigate(R.id.action_commonCompleteFragment_to_homeFragment)
            }
        }
        initView()
        onBackPressed()
    }

    private fun initView() {
        binding.apply {
            icon.setImageResource(args.icon)
            titleTv.text = args.title
            descriptionTv.text = args.description
            btnProceed.text = args.buttonTitle
        }
        when (args.type) {
            CompleteType.CHECKOUT -> {
                binding.btnProceed.setOnClickListener {
                    navController.navigate(CommonCompleteFragmentDirections
                        .actionCommonCompleteFragmentToOrderDetailFragment(args.id))
                }
            }
        }
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (prevDestinationId == R.id.checkoutFragment) {
                        navController.safeNavigate(R.id.action_commonCompleteFragment_to_homeFragment)
                    } else {
                        navController.popBackStack()
                    }
                }
            })
    }

}