package com.josephlimbert.weighttracker.model

class User {
    @JvmField
    var id: String? = null
    @JvmField
    var email: String? = null

    @JvmField
    var goalWeight: Float = 0f

    @JvmField
    var phone: String? = null
}
