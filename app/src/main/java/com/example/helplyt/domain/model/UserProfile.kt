package com.example.helplyt.domain.model

data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val postalCode: String = "",
    val city: String = "",
    val street: String = "",
    val number: String = "",
    val birthDate: String = ""

)

