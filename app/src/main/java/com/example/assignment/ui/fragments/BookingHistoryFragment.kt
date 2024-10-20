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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assignment.adapter.RideAdapter
import com.example.assignment.databinding.FragmentBookingHistoryBinding
import com.example.assignment.ui.MainActivity
import com.example.assignment.utils.Resource
import com.example.assignment.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class BookingHistoryFragment : Fragment() {
    lateinit var binding: FragmentBookingHistoryBinding
    lateinit var viewModel: MainViewModel
    private lateinit var rvAdapter: RideAdapter
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBookingHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        user = viewModel.getCurrentUser()!!

        rvAdapter = RideAdapter(emptyList())
        binding.rvHistory.layoutManager = LinearLayoutManager(context)
        binding.rvHistory.adapter = rvAdapter

        observeRidesResult()
    }

    private fun observeRidesResult() {
        viewModel.getAllRides(user.uid)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getRidesResult.collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            println("recyclerview data collection failed error ========= " + resource.data)
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
                            println("recyclerview data collection success ========= " + resource.data)
                            binding.progressBar.visibility = View.GONE
                            rvAdapter.updateRides(resource.data!!)
                        }
                    }

                }
            }
        }
    }

}