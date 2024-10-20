package com.example.assignment.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.assignment.repository.AuthRepository
import com.example.assignment.repository.MainRepository
import com.example.assignment.viewmodel.MainViewModel

class MainViewModelFactory(
    val authRepository: AuthRepository,
    val mainRepository: MainRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(authRepository, mainRepository) as T
    }
}