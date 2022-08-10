package com.vroomvroom.android.view.ui.home

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentFavoriteBinding
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.home.adapter.MerchantAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FavoriteFragment : BaseFragment<FragmentFavoriteBinding>(
    FragmentFavoriteBinding::inflate
) {
    private val merchantAdapter by lazy { MerchantAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeFavoriteMerchant()

        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()

        merchantAdapter.apply {
            setUser(mainActivityViewModel.user.value)
            binding.favoriteRv.adapter = this
        }

        merchantAdapter.onMerchantClicked = { merchant ->
            navController.navigate(
                FavoriteFragmentDirections.actionFavoriteFragmentToMerchantFragment(merchant.id)
            )
        }

        merchantAdapter.apply {
            onFavoriteClicked = { merchant, position, direction ->
                homeViewModel.updateFavorite(merchant.id)
                observeFavorite(this, merchant, position, direction)
            }
        }
    }

    private fun observeFavoriteMerchant() {
        mainViewModel.favorites.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    binding.favoriteRv.visibility = View.GONE
                    binding.commonNoticeLayout.hideNotice()
                    binding.favoriteShimmerLayout.visibility = View.VISIBLE
                    binding.favoriteShimmerLayout.startShimmer()
                }
                is Resource.Success -> {
                    val merchants = response.data
                    if (merchants.isEmpty()) {
                        binding.commonNoticeLayout.showNotice(
                            R.drawable.ic_favorites,
                            R.string.empty_favorite,
                            R.string.empty_favorite_detail,
                            null,
                            R.string.start_shopping
                        ) {
                            findNavController().popBackStack()
                        }
                    } else {
                        merchantAdapter.submitList(merchants.toMutableList())
                        binding.favoriteRv.visibility = View.VISIBLE
                    }
                    binding.favoriteShimmerLayout.visibility = View.GONE
                    binding.favoriteShimmerLayout.stopShimmer()
                }
                is Resource.Error -> {
                    binding.favoriteShimmerLayout.visibility = View.GONE
                    binding.favoriteShimmerLayout.stopShimmer()
                    binding.favoriteRv.visibility = View.GONE
                    binding.commonNoticeLayout.showNetworkError {
                        mainViewModel.getFavorites()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (view?.animation != null) {
            view?.animation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    mainViewModel.getFavorites()
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        } else {
            mainViewModel.getFavorites()
        }
    }
}