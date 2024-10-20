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
import com.example.assignment.databinding.FragmentForgotPasswordBinding
import com.example.assignment.ui.LoginActivity
import com.example.assignment.utils.Resource
import com.example.managerapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    lateinit var binding: FragmentForgotPasswordBinding
    lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as LoginActivity).viewModel

        binding.loginText.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.forgotPasswordImage.setImageResource(R.drawable.ic_profile)

        binding.forgotPasswordEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!emailValidation(s.toString())){
                    binding.forgotPasswordEmail.error = "Invalid email address"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.forgotPasswordBtn.setOnClickListener {
            handleForgotPassword()
        }
        observeForgotPasswordResult()
    }


    private fun emailValidation(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    private fun handleForgotPassword(){
        val email = binding.forgotPasswordEmail.text.toString().trim()
        if(!emailValidation(email)){
            binding.forgotPasswordEmail.error = "Invalid email address"
        }
        else{
            viewModel.forgotPassword(email)
        }
    }

    private fun observeForgotPasswordResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.forgotPasswordResult.collect{ resource ->
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
                            Toast.makeText(context,"Reset Password Mail Sent Successful", Toast.LENGTH_LONG).show()
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