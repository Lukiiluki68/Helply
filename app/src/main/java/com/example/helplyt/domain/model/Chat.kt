package com.example.helplyt.domain.model

data class Chat(
    val id: String = "",
    val userIds: List<String> = listOf(), // dwóch użytkowników
    val adTitle: String = "", // np. "Pomoc w przeprowadzce"
    val adId: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0
)
