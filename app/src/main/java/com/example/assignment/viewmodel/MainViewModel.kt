package com.example.assignment.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment.models.Ride
import com.example.assignment.models.User
import com.example.assignment.repository.AuthRepository
import com.example.assignment.repository.MainRepository
import com.example.assignment.utils.Resource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainViewModel(
    private val authRepository: AuthRepository,
    private val mainRepository: MainRepository
) : ViewModel() {

    private var _addRideResult = MutableStateFlow<Resource<DocumentReference>>(Resource.StandBy())
    val addRideResult = _addRideResult.asStateFlow()

    private var _getRidesResult = MutableStateFlow<Resource<List<Ride>>>(Resource.StandBy())
    val getRidesResult = _getRidesResult.asStateFlow()

    private var _getRideInfoResult =
        MutableStateFlow<Resource<DocumentSnapshot>>(Resource.StandBy())
    val getRideInfoResult = _getRideInfoResult.asStateFlow()

    private val _updateProfileResult = MutableStateFlow<Resource<Unit>>(Resource.StandBy())
    val updateProfileResult = _updateProfileResult.asStateFlow()

    private val _getProfileResult = MutableStateFlow<Resource<DocumentSnapshot>>(Resource.StandBy())
    val getProfileResult = _getProfileResult.asStateFlow()

    fun logout() {
        authRepository.logout()
    }

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    fun getUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _getProfileResult.value = Resource.Loading()
            mainRepository.getUserProfile(userId)
                .addOnSuccessListener { profile ->
                    _getProfileResult.value = Resource.Success(profile)
                    _getProfileResult.value = Resource.StandBy()
                }
                .addOnFailureListener { e ->
                    _getProfileResult.value =
                        Resource.Error(e.message ?: "Unable to get user profile")
                    _getProfileResult.value = Resource.StandBy()
                }
        }
    }

    fun updateUserProfile(userId: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateProfileResult.value = Resource.Loading()

            if (user.imageUri == null) {
                updateUserData(userId, user, user.imageUrl!!)
            } else {
                mainRepository.uploadImage(user.imageUri)
                    .catch {
                        _updateProfileResult.value = Resource.Error("Unable to upload image")
                        _updateProfileResult.value = Resource.StandBy()
                    }
                    .collect { url ->
                        updateUserData(userId, user, url)
                    }
            }
        }
    }

    private fun updateUserData(userId: String, user: User, url: String) {
        mainRepository.updateUserProfile(
            userId,
            User(user.email, user.uid, user.name, user.phone, null, url)
        )
            .addOnSuccessListener {
                _updateProfileResult.value = Resource.Success(Unit)
                _updateProfileResult.value = Resource.StandBy()
            }
            .addOnFailureListener { e ->
                _updateProfileResult.value =
                    Resource.Error(e.message ?: "Unable to update user profile")
                _updateProfileResult.value = Resource.StandBy()
            }
    }

    fun addRide(userId: String, ride: Ride) {
        viewModelScope.launch(Dispatchers.IO) {
            _addRideResult.value = Resource.Loading()
            mainRepository.addRide(userId, ride)
                .addOnSuccessListener { documentReference ->
                    mainRepository.updateRide(userId, documentReference.id)
                        .addOnSuccessListener {
                            _addRideResult.value = Resource.Success(documentReference)
                            _addRideResult.value = Resource.StandBy()
                        }
                        .addOnFailureListener { e ->
                            _addRideResult.value =
                                Resource.Error(e.message ?: "Unable to update ride")
                            _addRideResult.value = Resource.StandBy()
                        }
                }
                .addOnFailureListener { e ->
                    _addRideResult.value = Resource.Error(e.message ?: "Unable to add ride")
                    _addRideResult.value = Resource.StandBy()
                }
        }
    }

    fun getAllRides(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _getRidesResult.value = Resource.Loading()
            mainRepository.getAllRides(userId).collect { rides ->
//                Log.d("HomeFragment", items.toString())
                try {
                    Log.d("HistoryFragment", "in success")
                    val sortedRides = rides.sortedByDescending { it.timestamp?.toLongOrNull() }
                    val formattedRides = sortedRides.map { ride ->
                        ride.copy(
                            timestamp = convertEpochToDateTime(
                                ride.timestamp.toString().toLong()
                            )
                        )
                    }
                    _getRidesResult.value = Resource.Success(formattedRides)
                    delay(500)
                    _getRidesResult.value = Resource.StandBy()
                } catch (e: Exception) {
                    Log.d("HistoryFragment", "in error")
                    _getRidesResult.value =
                        Resource.Error(e.message ?: "Error occurred when getting all items")
                    delay(500)
                    _getRidesResult.value = Resource.StandBy()
                }
            }
        }
    }

    fun getRideInfo(userId: String, rideId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _getRideInfoResult.value = Resource.Loading()
            mainRepository.getRideInfo(userId, rideId)
                .addOnSuccessListener { ride ->
                    _getRideInfoResult.value = Resource.Success(ride)
                    _getRideInfoResult.value = Resource.StandBy()
                }
                .addOnFailureListener { e ->
                    _getRideInfoResult.value =
                        Resource.Error(e.message ?: "Unable to get ride information")
                    _getRideInfoResult.value = Resource.StandBy()
                }
        }
    }

    fun convertEpochToDateTime(epochSeconds: Long): String {
        val date = Date(epochSeconds * 1000)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }

}