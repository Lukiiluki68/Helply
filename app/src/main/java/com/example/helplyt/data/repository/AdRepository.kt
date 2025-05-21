package com.example.app.data.repository

import com.example.app.model.Ad

import com.google.firebase.firestore.FirebaseFirestore

interface AdRepository {
    suspend fun createAd(ad: Ad)
}
