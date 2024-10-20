package com.example.assignment.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.assignment.R
import com.example.assignment.databinding.FragmentConfirmationBinding
import com.example.assignment.models.Ride
import com.example.assignment.ui.MainActivity
import com.example.assignment.utils.Resource
import com.example.assignment.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class ConfirmationFragment : Fragment() {

    private lateinit var binding: FragmentConfirmationBinding
    private lateinit var viewModel: MainViewModel

    // Mock driver and vehicle data
    private val driverName = "Meet Mehta"
    private val carModel = "Toyota"
    private val licensePlate = "MH12 MM 1234"
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel

        user = viewModel.getCurrentUser()!!

        binding.driverName.text = driverName
        binding.carModel.text = carModel
        binding.licensePlate.text = licensePlate

        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        getRideInfo()

        observeRideResult()
    }

    private fun getRideInfo(){
        val rideId = arguments?.getString("rideId")
        viewModel.getRideInfo(user.uid,rideId!!)
    }

    private fun observeRideResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getRideInfoResult.collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            println("unable to store ride data " + resource.data)
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG)
                                .show()
                        }

                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is Resource.StandBy -> {
                            binding.progressBar.visibility = View.INVISIBLE
                        }

                        is Resource.Success -> {
                            println("ride data stored successfully " + resource.data)
                            binding.progressBar.visibility = View.GONE
                            val ride = resource.data!!.toObject(Ride::class.java)
                            binding.tvRideSource.text = ride!!.pickup
                            binding.tvRideDestination.text = ride.drop
                            binding.tvTimestamp.text = viewModel.convertEpochToDateTime(ride.timestamp!!.toLong())
                        }
                    }

                }
            }
        }
    }
}