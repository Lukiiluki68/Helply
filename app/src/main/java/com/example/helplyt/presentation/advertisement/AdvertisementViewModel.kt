import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class AdvertisementViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _ads = MutableStateFlow<List<Advertisement>>(emptyList())
    val ads: StateFlow<List<Advertisement>> = _ads

    init {
        fetchAdvertisements()
    }

    private fun fetchAdvertisements() {
        viewModelScope.launch {
            db.collection("ads")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val adList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Advertisement::class.java)?.copy(id = doc.id)
                    }
                    _ads.value = adList
                }
        }
    }
}
