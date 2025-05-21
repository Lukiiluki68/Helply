package com.example.app.data.repository

import com.example.app.model.Ad
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdRepository {

    override suspend fun createAd(ad: Ad) {
        firestore.collection("ads")
            .add(ad)
            .await()
    }
}
