package com.vroomvroom.android.view.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentAddressBottomSheetBinding
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.view.ui.activityviewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.location.viewmodel.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddressBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel by viewModels<LocationViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val args: AddressBottomSheetFragmentArgs by navArgs()

    private lateinit var binding: FragmentAddressBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddressBottomSheetBinding.inflate(inflater)
        return binding.root
    }

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
                    viewModel.insertLocation(UserLocationEntity(
                        address = street.toString(),
                        city = city.toString(),
                        addInfo = if (!addInfo.isNullOrBlank()) addInfo.toString() else null,
                        latitude = args.location.latitude,
                        longitude = args.location.longitude,
                        current_use = true
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
        if (activityViewModel.prevDestination == R.id.addressesFragment) {
            findNavController().navigate(R.id.action_addressBottomSheetFragment_to_addressesFragment)
        } else {
            findNavController().navigate(R.id.action_addressBottomSheetFragment_to_homeFragment)
        }
    }
}