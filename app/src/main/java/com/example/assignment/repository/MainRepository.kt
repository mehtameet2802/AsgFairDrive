package com.example.assignment.repository

import android.net.Uri
import android.util.Log
import com.example.assignment.models.Ride
import com.example.assignment.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class MainRepository {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance().reference


    fun addRide(userId: String, ride: Ride): Task<DocumentReference> {
        return firebaseDb
            .collection("users")
            .document(userId)
            .collection("rides")
            .add(ride)
    }

    fun updateRide(userId: String, rideId: String): Task<Void> {
        return firebaseDb
            .collection("users")
            .document(userId)
            .collection("rides")
            .document(rideId)
            .update(mapOf(Pair("ride_id", rideId)))
    }

    fun getRideInfo(userId: String, rideId: String): Task<DocumentSnapshot> {
        return firebaseDb
            .collection("users")
            .document(userId)
            .collection("rides")
            .document(rideId)
            .get()
    }

    fun getAllRides(userId: String): Flow<List<Ride>> = callbackFlow {
        val listenerItems = firebaseDb
            .collection("users")
            .document(userId)
            .collection("rides")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                querySnapshot?.let { data ->
                    Log.d("HistoryFragment", data.toString())
                    val rides = data.toObjects(Ride::class.java)
                    trySend(rides)
                }
            }
        awaitClose { listenerItems.remove() }
    }

    fun getUserProfile(userId: String): Task<DocumentSnapshot> {
        return firebaseDb
            .collection("users")
            .document(userId)
            .get()
    }

    fun updateUserProfile(userId: String, user: User): Task<Void> {
        return firebaseDb
            .collection("users")
            .document(userId)
            .update(
                mapOf(
                    Pair("imageUrl", user.imageUrl),
                    Pair("name", user.name),
                    Pair("email", user.email),
                    Pair("phone", user.phone),
                    Pair("uid", user.uid)
                )
            )
    }

    fun uploadImage(imageUri: Uri?): Flow<String> = callbackFlow {
        val imageRef = storageReference.child("profile_pictures/${UUID.randomUUID()}.jpg")

        imageUri?.let {
            val uploadTask = imageRef.putFile(it)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    trySend(uri.toString())
                    close()
                }
            }.addOnFailureListener { exception ->
                trySend("Unable to upload image")
                close(exception)
            }

            awaitClose {
                uploadTask.cancel()
            }
        } ?: run {
            trySend("Image URI is null")
            close()
        }
    }

}