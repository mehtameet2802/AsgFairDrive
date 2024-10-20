package com.example.managerapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment.AssignmentApplication
import com.example.assignment.models.User
import com.example.assignment.repository.AuthRepository
import com.example.assignment.utils.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    app: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(app) {

    private val _authResult = MutableStateFlow<Resource<FirebaseUser>>(Resource.StandBy())
    val authResult = _authResult.asStateFlow()

    private val _forgotPasswordResult = MutableStateFlow<Resource<Unit>>(Resource.StandBy())
    val forgotPasswordResult = _forgotPasswordResult.asStateFlow()

    fun signUp(email: String, password: String){
        viewModelScope.launch(Dispatchers.IO) {
            if(hasInternetConnection()){
                _authResult.value = Resource.Loading()
                authRepository.signUp(email, password)
                    .addOnSuccessListener { result ->
                        authRepository.addUserData(User(result.user!!.email!!,result.user!!.uid,"","",null,""))
                            .addOnSuccessListener {
                                _authResult.value = Resource.Success(result.user!!)
                                resetAuthResult()
                            }
                            .addOnFailureListener { e ->
                                _authResult.value =
                                    Resource.Error(e.message ?: "Contact Customer Care")
                                resetAuthResult()
                            }
                    }
                    .addOnFailureListener { e ->
                        _authResult.value =
                            Resource.Error(e.message ?: "An error occurred during signup")
                        resetAuthResult()
                    }
            } else{
                _authResult.value =Resource.Error("No Internet Connection")
                resetAuthResult()
            }
        }
    }

    fun logIn(email: String, password: String){
        if(hasInternetConnection()){
            viewModelScope.launch(Dispatchers.IO) {
                _authResult.value = Resource.Loading()
                authRepository.logIn(email, password)
                    .addOnSuccessListener { result ->
                        _authResult.value = Resource.Success(result.user!!)
                        resetAuthResult()
                    }
                    .addOnFailureListener { e ->
                        _authResult.value = Resource.Error(e.message ?: "An error occurred during login")
                        resetAuthResult()
                    }
            }
        } else{
            _authResult.value =Resource.Error("No Internet Connection")
            resetAuthResult()
        }
    }

    fun forgotPassword(email: String) {
        if(hasInternetConnection()){
            viewModelScope.launch(Dispatchers.IO) {
                _forgotPasswordResult.value = Resource.Loading()
                authRepository.forgotPassword(email)
                    .addOnSuccessListener {
                        _forgotPasswordResult.value = Resource.Success(Unit)
                        resetForgetPasswordResult()
                    }
                    .addOnFailureListener { e ->
                        _forgotPasswordResult.value =
                            Resource.Error(
                                e.message ?: "Error occurred while sending password reset mail"
                            )
                        resetForgetPasswordResult()
                    }
            }
        } else{
            _authResult.value = Resource.Error("No Internet Connection")
            resetAuthResult()
        }

    }


    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    fun getProfile(){

    }

    private fun resetAuthResult() {
        viewModelScope.launch {
            delay(500)
            _authResult.value = Resource.StandBy()
        }
    }

    private fun resetForgetPasswordResult() {
        viewModelScope.launch {
            delay(400)
            _forgotPasswordResult.value = Resource.StandBy()
        }
    }

    private fun hasInternetConnection():Boolean{
        val connectivityManager = getApplication<AssignmentApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
        else{
            connectivityManager.activeNetworkInfo?.run {
                return when(type){
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}