package com.example.helplyt.presentation.advertisement

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.helplyt.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreateAdViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun createAd(
        title: String,
        description: String,
        price: String,
        date: String,
        imageUris: List<Uri>,
        context: Context,
        location: String,
        onSuccess: (String) -> Unit
    ) {
        if (imageUris.isEmpty()) {
            saveAdToFirestore(title, description, price, date, emptyList(), location, onSuccess)
            return
        }

        val imageUrls = mutableListOf<String>()
        var uploadedCount = 0

        imageUris.forEach { uri ->
            val imageRef = storage.child("ads_images/${UUID.randomUUID()}")
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrls.add(downloadUri.toString())
                        uploadedCount++
                        if (uploadedCount == imageUris.size) {
                            saveAdToFirestore(title, description, price, date, imageUrls, location, onSuccess)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Błąd przesyłania zdjęcia", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveAdToFirestore(
        title: String,
        description: String,
        price: String,
        date: String,
        imageUrls: List<String>,
        location: String,
        onSuccess: (String) -> Unit
    ) {
        val ad = hashMapOf(
            "title" to title,
            "description" to description,
            "price" to price,
            "executionDate" to date,
            "imageUrls" to imageUrls,
            "userId" to userId,
            "location" to location,
            "acceptedUserId" to null,
            "applicantUserIds" to emptyList<String>(),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("ads")
            .add(ad)
            .addOnSuccessListener {
                Log.d("CreateAd", "Ogłoszenie zapisane: ${it.id}")
                onSuccess(it.id)
            }
            .addOnFailureListener {
                Log.e("CreateAd", "Błąd zapisu ogłoszenia", it)
            }
    }
    fun updateAd(
        adId: String,
        title: String,
        description: String,
        price: String,
        date: String,
        imageUris: List<Uri>,
        context: Context,
        location: String
    )
    {
        val existingUrls = imageUris.filter { it.toString().startsWith("http") }
        val newUris = imageUris.filter { !it.toString().startsWith("http") }

        if (newUris.isEmpty()) {
            updateAdInFirestore(
                adId,
                title,
                description,
                price = price,
                date = date,
                imageUrls = existingUrls.map { it.toString() },
                location = location
            )
            return
        }

        val newImageUrls = mutableListOf<String>()
        var uploadedCount = 0

        newUris.forEach { uri ->
            val imageRef = storage.child("ads_images/${UUID.randomUUID()}")
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        newImageUrls.add(downloadUri.toString())
                        uploadedCount++
                        if (uploadedCount == newUris.size) {
                            val allUrls = existingUrls.map { it.toString() } + newImageUrls
                            updateAdInFirestore(adId, title, description, price, date, allUrls, location)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Błąd przesyłania zdjęcia", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun updateAdInFirestore(
        adId: String,
        title: String,
        description: String,
        price: String,
        date: String,
        imageUrls: List<String>,
        location: String
    ) {
        val updatedAd = mapOf(
            "title" to title,
            "description" to description,
            "price" to price,
            "executionDate" to date,
            "imageUrls" to imageUrls,
            "location" to location,
            "acceptedUserId" to null,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("ads")
            .document(adId)
            .update(updatedAd)
            .addOnSuccessListener {
                Log.d("CreateAd", "Ogłoszenie zaktualizowane: $adId")
            }
            .addOnFailureListener {
                Log.e("CreateAd", "Błąd aktualizacji ogłoszenia", it)
            }
    }

    fun loadUserAddress(onResult: (UserProfile) -> Unit) {
        userId?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val profile = document.toObject(UserProfile::class.java)
                    if (profile != null) {
                        onResult(profile)
                    }
                }
                .addOnFailureListener {
                    Log.e("CreateAd", "Błąd pobierania profilu użytkownika", it)
                }
        }
    }
}
