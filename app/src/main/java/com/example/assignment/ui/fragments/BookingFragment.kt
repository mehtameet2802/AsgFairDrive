package com.example.assignment.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.assignment.R
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.assignment.databinding.FragmentBookingBinding
import com.example.assignment.models.DropOffLocation
import com.example.assignment.models.Ride
import com.example.assignment.ui.MainActivity
import com.example.assignment.utils.Resource
import com.example.assignment.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class BookingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var dropOffSpinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var binding: FragmentBookingBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private var currentLocation: LatLng? = null
    lateinit var viewModel: MainViewModel
    private lateinit var user: FirebaseUser

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


    private val dropoffLocations = listOf(
        DropOffLocation("Select", LatLng(40.7484, -73.9857)),
        DropOffLocation("Taj Mahal, Agra", LatLng(27.1751, 78.0421)),
        DropOffLocation("India Gate, Mumbai", LatLng(18.9219, 72.8347)),
        DropOffLocation("Qutub Minar, Delhi", LatLng(28.5245, 77.1855))
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBookingBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel

        user = viewModel.getCurrentUser()!!

        dropOffSpinner = binding.dropOffSpinner
        submitButton = binding.submitRide

        binding.btnCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }

        setupSpinner()
        setupSubmitButton()

        dropOffSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDropoff = dropoffLocations[position]

                if (selectedDropoff.name == "Select") {
                    currentLocation?.let {
                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(it).title("Current Location"))
                    }
                }
                else{
                    moveToDestination(selectedDropoff.latLng,selectedDropoff.name)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        observeRideResult()

    }


    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dropoffLocations.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropOffSpinner.adapter = adapter
    }

    private fun resetSpinner() {
        dropOffSpinner.setSelection(0)
    }


    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            if (currentLocation == null) {
                Toast.makeText(context, "Current location not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedDropoff = dropoffLocations[dropOffSpinner.selectedItemPosition]

            if (selectedDropoff.name == "Select") {
                Toast.makeText(
                    context,
                    "Please select a valid dropoff location",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            submitRideRequest(currentLocation!!, selectedDropoff) // Proceed with valid selection
        }
    }

    private fun getCurrentLocation() {
        if (!isLocationEnabled(requireContext()))
            promptEnableLocation(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLocation = LatLng(location.latitude, location.longitude)
                moveToCurrentLocation()
            } else {
                Toast.makeText(context, "Unable to retrieve current location", Toast.LENGTH_SHORT)
                    .show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = false
        getCurrentLocation()
    }

    private fun moveToCurrentLocation() {
        currentLocation?.let {
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(it).title("Current Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    private fun moveToDestination(latLng: LatLng,placeName:String) {
        val map = mMap

        map.clear()

        currentLocation?.let {
            map.addMarker(MarkerOptions().position(it).title("Current Location"))
        }

        mMap.addMarker(MarkerOptions().position(latLng).title(placeName))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun submitRideRequest(pickup: LatLng, dropoff: DropOffLocation) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        val timeStamp = (System.currentTimeMillis() / 1000).toString()
        viewModel.addRide(user.uid, Ride("","Home/Your Location",dropoff.name,timeStamp))
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun promptEnableLocation(context: Context) {
        AlertDialog.Builder(context)
            .setMessage("Location services are disabled. Please enable them to proceed.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeRideResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addRideResult.collect { resource ->
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
                            Toast.makeText(requireContext(),"Ride Confirmed", Toast.LENGTH_LONG).show()
                            resetSpinner()
                            moveToCurrentLocation()
                            val bundle = Bundle()
                            bundle.putString("rideId", resource.data!!.id)
                            findNavController().navigate(R.id.action_bookingFragment_to_confirmationFragment,bundle)
                        }
                    }

                }
            }
        }
    }



}