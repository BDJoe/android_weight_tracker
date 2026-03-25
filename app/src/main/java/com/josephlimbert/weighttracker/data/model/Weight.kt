package com.josephlimbert.weighttracker.data.model

import com.google.firebase.Timestamp

data class Weight(
    val id: String = "",
    val weight: Double = 0.0,
    val recordedDate: Timestamp = Timestamp.now(),
    val userId: String = ""
)
