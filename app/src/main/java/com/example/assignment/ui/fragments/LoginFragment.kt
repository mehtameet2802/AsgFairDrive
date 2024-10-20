package com.example.assignment.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.example.assignment.databinding.FragmentLoginBinding
import com.example.assignment.ui.LoginActivity
import com.example.assignment.ui.MainActivity
import com.example.assignment.utils.Resource
import com.example.managerapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding
    lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as LoginActivity).viewModel

        binding.registerText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        binding.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.loginImage.setImageResource(R.drawable.ic_profile)

        binding.loginEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!emailValidation(s.toString())) {
                    binding.loginEmail.error = "Invalid email address"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.loginPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!passwordValidation(s.toString())) {
                    binding.loginPassword.error = "Invalid password"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.loginBtn.setOnClickListener {
            handleLogin()
        }
        observeAuthResult()
    }

    private fun emailValidation(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    private fun passwordValidation(password: String): Boolean {
        return password.trim().length >= 8
    }

    private fun handleLogin() {
        val email = binding.loginEmail.text.toString().trim()
        val password = binding.loginPassword.text.toString().trim()
        if (!emailValidation(email)) {
            binding.loginEmail.error = "Invalid email address"
        } else if (!passwordValidation(password)) {
            binding.loginPassword.error = "Invalid password"
        } else {
            viewModel.logIn(email, password)
        }
    }

    private fun observeAuthResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authResult.collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            println("logged in failed collecting error ========= " + resource.data)
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        }

                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            println("logged in successfully ========= " + resource.data)
                            Log.d("LoginFragment", resource.data.toString())
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_LONG).show()
                            startActivity(Intent(requireActivity(), MainActivity::class.java))
                            requireActivity().finish()
                        }
                        is Resource.StandBy -> {
                            binding.progressBar.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }
}