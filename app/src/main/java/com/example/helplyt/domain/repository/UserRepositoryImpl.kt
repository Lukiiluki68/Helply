package com.example.app.data.repository

import android.net.Uri
import com.example.app.domain.repository.UserRepository
import com.example.helplyt.domain.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl : UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()


    override suspend fun saveUserProfile(profile: UserProfile) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val data = mapOf(
            "username" to profile.username,
            "email" to profile.email,
            "avatarUrl" to profile.avatarUrl,
            "postalCode" to profile.postalCode,
            "city" to profile.city,
            "street" to profile.street,
            "number" to profile.number,
            "birthDate" to profile.birthDate // dodane, jeśli masz to w modelu
        )
        firestore.collection("users").document(userId).set(data, SetOptions.merge()).await()
    }

    override suspend fun getUserProfile(): UserProfile? {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val doc = firestore.collection("users").document(userId).get().await()
        return if (doc.exists()) {
            UserProfile(
                username = doc.getString("username") ?: "",
                email = doc.getString("email") ?: "",
                avatarUrl = doc.getString("avatarUrl"),
                postalCode = doc.getString("postalCode") ?: "",
                city = doc.getString("city") ?: "",
                street = doc.getString("street") ?: "",
                number = doc.getString("number") ?: "",
                birthDate = doc.getString("birthDate") ?: "" // jeśli to masz
            )
        } else null
    }

    override suspend fun uploadAvatar(uri: Uri): Uri? {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val avatarRef = storage.child("avatars/$userId.jpg")
        avatarRef.putFile(uri).await()
        return avatarRef.downloadUrl.await()
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: throw Exception("Brak użytkownika")
        val email = user.email ?: throw Exception("Brak adresu email")

        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(credential).await() // ⬅️ WYMAGANE
        user.updatePassword(newPassword).await()
    }

}

