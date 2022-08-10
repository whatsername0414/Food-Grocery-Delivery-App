package com.vroomvroom.android.view.ui.location

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAddressBottomSheetBinding
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddressBottomSheetFragment : BaseBottomSheetFragment<FragmentAddressBottomSheetBinding>(
    FragmentAddressBottomSheetBinding::inflate
) {

    private val args: AddressBottomSheetFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationViewModel.userLocation.observe(viewLifecycleOwner){}

        binding.localityInputEditText.setText(args.location.city)
        binding.btnSave.setOnClickListener {
            val street = binding.streetInputEditText.text
            val city = binding.localityInputEditText.text
            val addInfo = binding.addInfoInputEditText.text
            if (!street.isNullOrBlank()) {
                if (!city.isNullOrBlank()) {
                    locationViewModel.insertLocation(
                        LocationEntity(
                        address = street.toString(),
                        city = city.toString(),
                        addInfo = addInfo?.toString(),
                        latitude = args.location.latitude,
                        longitude = args.location.longitude,
                        currentUse = true))
                    navigate()
                } else {
                    binding.localityInputLayout.helperText = "required"
                }
            } else {
                binding.streetInputLayout.helperText = "required"
            }
        }
    }

    private fun navigate() {
        if (mainActivityViewModel.prevDestination == R.id.addressesFragment) {
            findNavController().navigate(AddressBottomSheetFragmentDirections.
                actionAddressBottomSheetFragmentToAddressesFragment(null))
        } else {
            findNavController().navigate(R.id.action_addressBottomSheetFragment_to_homeFragment)
        }
    }
}