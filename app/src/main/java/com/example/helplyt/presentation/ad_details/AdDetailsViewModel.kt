package com.example.helplyt.presentation.ad_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.AdRepositoryImpl
import com.example.helplyt.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Ad(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val executionDate: String = "",
    val location: String = "",
    val userId: String = "",
    val imageUrls: List<String> = emptyList(),
    val imageUrl: String = "",
    val acceptedUserId: String? = null,
    val applicantUserIds: List<String> = emptyList()
)


class AdDetailsViewModel(private val adId: String) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val repository = AdRepositoryImpl()
    private val _adData = MutableStateFlow<Ad?>(null)
    val adData: StateFlow<Ad?> = _adData.asStateFlow()
    private val _applicantUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val applicantUsers: StateFlow<List<UserProfile>> = _applicantUsers.asStateFlow()
    private val _userData = MutableStateFlow<UserProfile?>(null)
    val userData: StateFlow<UserProfile?> = _userData.asStateFlow()

    init {
        loadAdDetails()
    }

    private fun loadAdDetails() {
        viewModelScope.launch {
            db.collection("ads").document(adId).get()
                .addOnSuccessListener { adDoc ->
                    val rawAd = adDoc.toObject(Ad::class.java)
                    val safeAd = rawAd?.copy(
                        applicantUserIds = rawAd.applicantUserIds ?: emptyList()
                    )
                    _adData.value = safeAd
                    val applicantIds = safeAd?.applicantUserIds ?: emptyList()
                    if (applicantIds.isNotEmpty()) {
                        db.collection("users")
                            .whereIn(FieldPath.documentId(), applicantIds)
                            .get()
                            .addOnSuccessListener { result ->
                                val users = result.documents.mapNotNull { doc ->
                                    doc.toObject(UserProfile::class.java)?.copy(userId = doc.id)
                                }
                                _applicantUsers.value = users
                            }
                    }

                    val userId = rawAd?.userId ?: return@addOnSuccessListener
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val user = userDoc.toObject(UserProfile::class.java)
                            _userData.value = user
                        }
                }
        }
    }
    fun applyForAd(adId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("ads").document(adId)
            .update("applicantUserIds", FieldValue.arrayUnion(userId))
    }

    fun cancelApplication(adId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("ads").document(adId)
            .update("applicantUserIds", FieldValue.arrayRemove(userId))
    }

    fun acceptUserForAd(adId: String, userIdToAccept: String) {
        db.collection("ads").document(adId)
            .update("acceptedUserId", userIdToAccept)
    }

    fun reload() {
        loadAdDetails()
    }
    fun deleteAdWithImages(adId: String, imageUrls: List<String>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
            val db = FirebaseFirestore.getInstance()

            // Usuń zdjęcia jedno po drugim
            imageUrls.forEach { imageUrl ->
                val storageRef = storage.getReferenceFromUrl(imageUrl)
                storageRef.delete().addOnFailureListener {
                    // Loguj błędy jeśli chcesz
                }
            }

            // Usuń dokument ogłoszenia po usunięciu zdjęć
            db.collection("ads").document(adId).delete().addOnSuccessListener {
                onSuccess()
            }
        }
    }

}

class AdDetailsViewModelFactory(private val adId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdDetailsViewModel(adId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
