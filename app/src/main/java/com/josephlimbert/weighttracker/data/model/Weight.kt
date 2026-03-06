package com.josephlimbert.weighttracker.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Weight(
    @DocumentId val id: String = "",
    val weight: Double = 0.0,
    val recordedDate: Timestamp = Timestamp.now(),
    val userId: String = ""
)
