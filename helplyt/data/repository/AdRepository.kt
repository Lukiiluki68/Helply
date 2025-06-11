package com.example.app.data.repository

import Ad

import com.google.firebase.firestore.FirebaseFirestore

interface AdRepository {
    suspend fun createAd(ad: Ad)
    suspend fun acceptAd(adId: String, acceptedUserId: String)
}
