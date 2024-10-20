package com.example.assignment.repository

import com.example.assignment.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDb = FirebaseFirestore.getInstance()

    fun signUp(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
    }

    fun logIn(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

    fun forgotPassword(email: String): Task<Void> {
        return firebaseAuth.sendPasswordResetEmail(email)
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun addUserData(user: User): Task<Void> {
        return firebaseDb.collection("users").document(user.uid!!).set(user)
    }
}