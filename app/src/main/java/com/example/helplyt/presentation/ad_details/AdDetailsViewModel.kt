package com.example.helplyt.presentation.ad_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.AdRepositoryImpl
import com.example.helplyt.domain.model.UserProfile
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
    val acceptedUserId: String? = null
)


class AdDetailsViewModel(private val adId: String) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val repository = AdRepositoryImpl()
    private val _adData = MutableStateFlow<Ad?>(null)
    val adData: StateFlow<Ad?> = _adData.asStateFlow()

    private val _userData = MutableStateFlow<UserProfile?>(null)
    val userData: StateFlow<UserProfile?> = _userData.asStateFlow()

    init {
        loadAdDetails()
    }

    private fun loadAdDetails() {
        viewModelScope.launch {
            db.collection("ads").document(adId).get()
                .addOnSuccessListener { adDoc ->
                    val ad = adDoc.toObject(Ad::class.java)
                    _adData.value = ad

                    val userId = ad?.userId ?: return@addOnSuccessListener
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val user = userDoc.toObject(UserProfile::class.java)
                            _userData.value = user
                        }
                }
        }
    }
    fun acceptAd(adId: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.acceptAd(adId, userId)
                onSuccess()
            } catch (e: Exception) {
                // Obsługa błędu np. Log.e(...)
            }
        }
    }
    fun reload() {
        loadAdDetails()
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
