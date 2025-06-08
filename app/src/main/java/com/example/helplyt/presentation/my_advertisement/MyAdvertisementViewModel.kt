package com.example.helplyt.presentation.my_advertisement

import Advertisement
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyAdvertisementViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _myOwnAds = MutableStateFlow<List<Advertisement>>(emptyList())
    val myOwnAds: StateFlow<List<Advertisement>> = _myOwnAds

    private val _myApplications = MutableStateFlow<List<Advertisement>>(emptyList())
    val myApplications: StateFlow<List<Advertisement>> = _myApplications

    init {
        fetchMyOwnAds()
        fetchMyApplications()
    }

    fun fetchMyOwnAds() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("ads")
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val adList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Advertisement::class.java)?.copy(id = doc.id)
                }
                _myOwnAds.value = adList
            }
    }

    fun fetchMyApplications() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("ads")
            .whereEqualTo("acceptedUserId", currentUserId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val adList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Advertisement::class.java)?.copy(id = doc.id)
                }
                _myApplications.value = adList
            }
    }

    fun reload() {
        fetchMyOwnAds()
        fetchMyApplications()
    }
}
