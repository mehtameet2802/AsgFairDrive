package com.example.assignment.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.assignment.R
import com.example.assignment.databinding.ActivityMainBinding
import com.example.assignment.repository.AuthRepository
import com.example.assignment.repository.MainRepository
import com.example.assignment.viewmodel.MainViewModel
import com.example.assignment.viewmodel.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val authRepository = AuthRepository()
        val mainRepository = MainRepository()
        val viewModelProviderFactory = MainViewModelFactory(authRepository, mainRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[MainViewModel::class.java]

        val navController =
            supportFragmentManager.findFragmentById(R.id.mainNavHostFragment)?.findNavController()
        binding.bottomNavigationView.setupWithNavController(navController!!)


    }
}