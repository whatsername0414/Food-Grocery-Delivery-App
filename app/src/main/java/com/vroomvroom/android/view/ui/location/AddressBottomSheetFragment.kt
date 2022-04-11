package com.vroomvroom.android.view.ui.location

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAddressBottomSheetBinding
import com.vroomvroom.android.domain.db.user.UserLocationEntity
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

        binding.localityInputEditText.setText(args.location.city)
        val args = args
        binding.btnSave.setOnClickListener {
            val street = binding.streetInputEditText.text
            val city = binding.localityInputEditText.text
            val addInfo = binding.addInfoInputEditText.text
            if (!street.isNullOrBlank()) {
                if (!city.isNullOrBlank()) {
                    locationViewModel.insertLocation(UserLocationEntity(
                        address = street.toString(),
                        city = city.toString(),
                        addInfo = if (!addInfo.isNullOrBlank()) addInfo.toString() else null,
                        latitude = args.location.latitude,
                        longitude = args.location.longitude,
                        currentUse = true
                        ))
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
            findNavController().navigate(R.id.action_addressBottomSheetFragment_to_addressesFragment)
        } else {
            findNavController().navigate(R.id.action_addressBottomSheetFragment_to_homeFragment)
        }
    }
}