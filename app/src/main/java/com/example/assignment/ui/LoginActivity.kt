package com.example.assignment.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.assignment.R
import com.example.assignment.databinding.ActivityLoginBinding
import com.example.assignment.repository.AuthRepository
import com.example.managerapp.viewmodel.AuthViewModel
import com.example.managerapp.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    lateinit var viewModel: AuthViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val authRepository = AuthRepository()
        val viewModelFactory = AuthViewModelFactory(application,authRepository)
        viewModel = ViewModelProvider(this,viewModelFactory)[AuthViewModel::class.java]

        navController = supportFragmentManager.findFragmentById(R.id.loginNavHostFragment)
            ?.findNavController()!!

        if(viewModel.getCurrentUser()!=null){
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }
}