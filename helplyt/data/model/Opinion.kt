package com.example.helplyt.model.model

data class Opinion(
    val dateAdded: String = "",
    var authorName: String? = null,
    val rating: Float = 0f,
    val content: String = "",
    val recipientUserId: String = "",
    val date: String = "",
    val authorUserId: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
)
