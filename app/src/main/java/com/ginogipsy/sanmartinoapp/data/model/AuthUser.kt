package com.ginogipsy.sanmartinoapp.data.model

data class AuthUser(
    val userId: String,
    val username: String,
    val email: String = "",
    val displayName: String = username,
)
