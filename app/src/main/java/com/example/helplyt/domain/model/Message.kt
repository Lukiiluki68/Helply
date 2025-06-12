package com.example.helplyt.domain.model

data class Message(
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val imageUrl: String? = null,
    val seenBy: List<String> = listOf()
)
