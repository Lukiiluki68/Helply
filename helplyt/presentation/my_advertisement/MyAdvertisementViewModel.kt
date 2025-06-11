package com.example.helplyt.presentation.my_advertisement

import Advertisement
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helplyt.model.model.Opinion
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

    private val _myReceivedOpinions = MutableStateFlow<List<Opinion>>(emptyList())
    val myReceivedOpinions: StateFlow<List<Opinion>> = _myReceivedOpinions

    init {
        fetchMyOwnAds()
        fetchMyApplications()
        loadMyReceivedOpinions()
    }
    fun loadMyReceivedOpinions() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("opinions")
            .whereEqualTo("targetUserId", uid)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val opinions = snapshots.documents.mapNotNull { it.toObject(Opinion::class.java) }.toMutableList()

                    if (opinions.isEmpty()) {
                        _myReceivedOpinions.value = emptyList()
                        return@addSnapshotListener
                    }
                    for ((index, opinion) in opinions.withIndex()) {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(opinion.authorUserId)
                            .get()
                            .addOnSuccessListener { doc ->
                                val username = doc.getString("username") ?: "Nieznany"
                                opinions[index] = opinion.copy(authorName = username)
                                _myReceivedOpinions.value = opinions.toList()
                            }
                    }
                }
            }
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
