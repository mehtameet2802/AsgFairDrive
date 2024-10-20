package com.example.assignment.models

import android.net.Uri

data class User(
    val email: String?=null,
    val uid: String?=null,
    val name:String?=null,
    val phone:String?=null,
    val imageUri: Uri?=null,
    val imageUrl:String?=null
)