package com.josephlimbert.weighttracker.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val goalWeight: Double = 0.0,
    val weightUnit: String = "lbs"
)