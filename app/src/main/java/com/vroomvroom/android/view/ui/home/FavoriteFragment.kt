package com.vroomvroom.android.view.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.vroomvroom.android.databinding.FragmentFavoriteBinding
import com.vroomvroom.android.domain.model.merchant.MerchantData
import com.vroomvroom.android.utils.Utils.updateAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.home.adapter.MerchantAdapter
import com.vroomvroom.android.view.ui.activityviewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FavoriteFragment : Fragment() {

    private val viewModel by viewModels<HomeViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val merchantAdapter by lazy { MerchantAdapter(true) }

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.favoriteRv.adapter = merchantAdapter

        viewModel.favoriteMerchant()
        observeFavoriteMerchant()

        merchantAdapter.onMerchantClicked = { merchant ->
            FavoriteFragmentDirections.actionFavoriteFragmentToMerchantFragment(merchant._id)
        }

        merchantAdapter.onFavoriteClicked = { merchant, direction ->
            viewModel.favorite(merchant._id, direction)
            activityViewModel.favoriteDirection = direction
            observeFavorite(merchant)
        }
    }

    private fun observeFavoriteMerchant() {
        viewModel.favoriteMerchants.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.favoriteRv.visibility = View.GONE
                    binding.emptyFavorite.visibility = View.GONE
                    binding.favoriteShimmerLayout.visibility = View.VISIBLE
                    binding.favoriteShimmerLayout.startShimmer()
                }
                is ViewState.Success -> {
                    val merchants = response.result.data
                    if (merchants.isNullOrEmpty()) {
                        binding.emptyFavorite.visibility = View.VISIBLE
                        merchantAdapter.setData(mutableListOf())
                    } else {
                        merchantAdapter.setData(merchants)
                        binding.emptyFavorite.visibility = View.GONE
                        binding.favoriteRv.visibility = View.VISIBLE
                    }
                    binding.favoriteShimmerLayout.visibility = View.GONE
                    binding.favoriteShimmerLayout.stopShimmer()
                }
                is ViewState.Error -> {
                    binding.favoriteShimmerLayout.visibility = View.GONE
                    binding.favoriteShimmerLayout.stopShimmer()
                    binding.favoriteRv.visibility = View.GONE
                    binding.emptyFavorite.visibility = View.GONE
                    binding.favoriteConnectionFailedNotice.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun observeFavorite(merchant: MerchantData) {
        viewModel.favorite.observe(viewLifecycleOwner, { response ->
            val direction = activityViewModel.favoriteDirection
            when(response) {
                is ViewState.Loading -> Unit
                is ViewState.Success -> {
                    if (direction == 1) {
                        merchantAdapter.updateAdapter(merchant, true)
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Removed from favorites",
                            Snackbar.LENGTH_LONG
                        ).show()
                        merchantAdapter.updateAdapter(merchant, false)
                    }
                    viewModel.favorite.removeObservers(viewLifecycleOwner)
                }
                is ViewState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (direction == 1) {
                        merchantAdapter.updateAdapter(merchant, true)
                    } else {
                        merchantAdapter.updateAdapter(merchant, false)
                    }
                    viewModel.favorite.removeObservers(viewLifecycleOwner)
                }
            }
        })
    }
}