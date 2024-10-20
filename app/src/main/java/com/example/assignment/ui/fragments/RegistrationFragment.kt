package com.example.assignment.ui.fragments

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
import com.example.assignment.databinding.FragmentRegistrationBinding
import com.example.assignment.ui.LoginActivity
import com.example.assignment.utils.Resource
import com.example.managerapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch


class RegistrationFragment : Fragment() {

    lateinit var binding: FragmentRegistrationBinding
    lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRegistrationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as LoginActivity).viewModel

        binding.loginText.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.registerImage.setImageResource(R.drawable.ic_profile)

        binding.registerEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!emailValidation(s.toString())){
                    binding.registerEmail.error = "Invalid email address"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.registerPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!passwordValidation(s.toString(),null)){
                    binding.registerPassword.error = "Invalid password"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.registerConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = binding.registerPassword.text.toString()
                if(password==""){
                    binding.registerPassword.error = "Enter password"
                }
                else if(!passwordValidation(password,s.toString())){
                    binding.registerPassword.error = "Invalid password"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.registerBtn.setOnClickListener {
            handleRegistration()
        }
        observeAuthResult()
    }

    private fun emailValidation(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    private fun passwordValidation(password: String, confirmPassword: String?): Boolean {
        if (confirmPassword != null) {
            return (password == confirmPassword && password.length >= 8)
        }
        return password.length >= 8
    }

    private fun handleRegistration(){
        val email = binding.registerEmail.text.toString().trim()
        val password = binding.registerPassword.text.toString().trim()
        val confirmPassword = binding.registerConfirmPassword.text.toString().trim()
        if(!emailValidation(email)){
            binding.registerEmail.error = "Invalid email address"
        }
        else if(!passwordValidation(password,confirmPassword)){
            binding.registerPassword.error = "Invalid password"
            binding.registerConfirmPassword.error = "Invalid password"
        }
        else{
            viewModel.signUp(email,password)
        }
    }

    private fun observeAuthResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authResult.collect{ resource ->
                    when(resource){
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context,resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            Log.d("Login",resource.data.toString())
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context,"Registered Successful", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.loginFragment)
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