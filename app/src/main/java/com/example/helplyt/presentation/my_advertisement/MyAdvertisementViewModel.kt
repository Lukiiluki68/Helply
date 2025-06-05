package com.example.helplyt.presentation.my_advertisement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Advertisement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val executionDate: String = "",
    val imageUrl: String? = null,
    val userId: String = "",
    val timestamp: Long = 0
)

class MyAdvertisementViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _myAds = MutableStateFlow<List<Advertisement>>(emptyList())
    val myAds: StateFlow<List<Advertisement>> = _myAds

    init {
        fetchMyAdvertisements()
    }

    private fun fetchMyAdvertisements() {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            db.collection("ads")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val adList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Advertisement::class.java)?.copy(id = doc.id)
                    }
                    _myAds.value = adList
                }
        }
    }
}
