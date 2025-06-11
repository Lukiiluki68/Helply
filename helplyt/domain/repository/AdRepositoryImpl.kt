package com.example.app.data.repository

import Ad
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
    override suspend fun acceptAd(adId: String, acceptedUserId: String) {
        firestore.collection("ads")
            .document(adId)
            .update("acceptedUserId", acceptedUserId)
            .await()
    }
}
