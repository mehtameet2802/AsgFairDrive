package com.example.assignment.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.bumptech.glide.Glide
import com.example.assignment.R
import com.example.assignment.databinding.FragmentProfileBinding
import com.example.assignment.models.User
import com.example.assignment.ui.LoginActivity
import com.example.assignment.ui.MainActivity
import com.example.assignment.utils.Resource
import com.example.assignment.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID


class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding
    lateinit var viewModel: MainViewModel
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private lateinit var user: FirebaseUser
    private var userUrl:String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        user = viewModel.getCurrentUser()!!

        binding.etEmail.setText(user.email.toString())

        binding.logoutButton.setOnClickListener {
            logOut()
        }

        binding.btnChangePicture.setOnClickListener {
            selectImage()
        }

        binding.btnSaveProfile.setOnClickListener {
            updateProfile()
        }

        loadProfile()
        observeGetProfile()
        observeUpdateProfile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.ivProfilePicture.setImageURI(imageUri)
        }
    }

    private fun logOut() {
        viewModel.logout()
        startActivity(Intent(requireActivity(), LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun updateProfile() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val phone = binding.etPhone.text.toString()

        if (imageUri == null && userUrl == "") {
            Toast.makeText(requireContext(), "Please select profile image", Toast.LENGTH_SHORT)
                .show()
        } else if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter name", Toast.LENGTH_SHORT).show()
        } else if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter email", Toast.LENGTH_SHORT).show()
        } else if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter phone number", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updateUserProfile(user.uid, User(email, user.uid, name, phone, imageUri, userUrl))
        }
    }

    private fun loadProfile() {
        viewModel.getUserProfile(user.uid)
    }

    private fun observeGetProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getProfileResult.collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            println("unable to get user data " + resource.data)
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
                            println("user data retrieved successfully " + resource.data)
                            val user = resource.data!!.toObject(User::class.java)
                            if (user!!.name != "")
                                binding.etName.setText(user.name)
                            if (user.phone != "")
                                binding.etPhone.setText(user.phone)
                            binding.etEmail.setText(user.email)

                            if (user.imageUrl != "") {
                                userUrl = user.imageUrl.toString()
                                Glide.with(requireContext()).load(user.imageUrl)
                                    .into(binding.ivProfilePicture)
                            }
                            binding.progressBar.visibility = View.GONE
                        }
                    }

                }
            }
        }
    }

    private fun observeUpdateProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateProfileResult.collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            println("unable to update user profile " + resource.data)
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
                            println("user profile updated successfully " + resource.data)
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_LONG)
                                .show()
                            binding.progressBar.visibility = View.GONE
                        }
                    }

                }
            }
        }
    }
}