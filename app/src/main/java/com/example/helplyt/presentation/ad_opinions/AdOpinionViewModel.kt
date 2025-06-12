package com.example.helplyt.presentation.user_opinions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helplyt.model.model.Opinion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserOpinionsViewModel(private val userId: String) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _opinions = MutableStateFlow<List<Opinion>>(emptyList())
    val opinions: StateFlow<List<Opinion>> = _opinions.asStateFlow()

    init {
        loadOpinions()
    }

    private fun loadOpinions() {
        viewModelScope.launch {
            db.collection("opinions")
                .whereEqualTo("recipientUserId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val opinionsList = querySnapshot.documents.mapNotNull {
                        it.toObject(Opinion::class.java)
                    }
                    _opinions.value = opinionsList
                }
        }
    }
}

// 🏭 ViewModel Factory w tym samym pliku
class UserOpinionsViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserOpinionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserOpinionsViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
