package com.example.assignment.models

import com.google.android.gms.maps.model.LatLng

data class DropOffLocation(
    val name: String,
    val latLng: LatLng
)