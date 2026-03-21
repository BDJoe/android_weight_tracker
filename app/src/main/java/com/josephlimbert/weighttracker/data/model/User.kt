package com.josephlimbert.weighttracker.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    val id: String = "",
    val email: String = "",
    val goalWeight: Double = 0.0,
    val weightUnit: String = "lbs"
)