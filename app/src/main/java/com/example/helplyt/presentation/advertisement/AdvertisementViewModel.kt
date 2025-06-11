import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class SortOrder {
    NEWEST,
    OLDEST,
    PRICE_ASC,
    PRICE_DESC
}

data class Advertisement(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val executionDate: String = "",
    val imageUrls: List<String> = emptyList(),
    val userId: String = "",
    val acceptedUserId: String? = null,
    val applicantUserIds: List<String> = emptyList(),
    val location: String = "",
    val timestamp: Long = 0

){
    val mainImageUrl: String?
        get() = imageUrls.firstOrNull()
}

data class FilterState(
    val priceMin: Int? = null,
    val priceMax: Int? = null,
    val withImageOnly: Boolean = false
)


class AdvertisementViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState
    private var allAds: List<Advertisement> = emptyList()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _ads = MutableStateFlow<List<Advertisement>>(emptyList())
    val ads: StateFlow<List<Advertisement>> = _ads

    init {
        fetchAdvertisements()
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        applySorting()
    }
    fun setFilter(priceMin: Int?, priceMax: Int?, withImageOnly: Boolean) {
        _filterState.value = FilterState(priceMin, priceMax, withImageOnly)
        applyFilters()
    }

    private fun fetchAdvertisements() {
        viewModelScope.launch {
            db.collection("ads")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    val adList = snapshot.documents.mapNotNull { doc ->
                        val ad = doc.toObject(Advertisement::class.java)
                        ad?.copy(
                            id = doc.id,
                            applicantUserIds = ad.applicantUserIds ?: emptyList()
                        )
                    }
                        .filter { ad ->
                        ad.userId != currentUserId && ad.acceptedUserId == null
                    }

                    allAds = adList
                    applyFilters()
                }
        }
    }

    private fun applySorting() {
        val currentAds = _ads.value
        val sortedAds = when (_sortOrder.value) {
            SortOrder.NEWEST -> currentAds.sortedByDescending { it.timestamp }
            SortOrder.OLDEST -> currentAds.sortedBy { it.timestamp }
            SortOrder.PRICE_ASC -> currentAds.sortedBy { it.price.toIntOrNull() ?: 0 }
            SortOrder.PRICE_DESC -> currentAds.sortedByDescending { it.price.toIntOrNull() ?: 0 }
        }
        _ads.value = sortedAds
    }
    private fun applyFilters() {
        val filters = _filterState.value
        val sortedAds = when (_sortOrder.value) {
            SortOrder.NEWEST -> allAds.sortedByDescending { it.timestamp }
            SortOrder.OLDEST -> allAds.sortedBy { it.timestamp }
            SortOrder.PRICE_ASC -> allAds.sortedBy { it.price.toIntOrNull() ?: 0 }
            SortOrder.PRICE_DESC -> allAds.sortedByDescending { it.price.toIntOrNull() ?: 0 }
        }

        val filtered = sortedAds.filter { ad ->
            val price = ad.price.toIntOrNull() ?: return@filter false

            val matchesMin = filters.priceMin?.let { price >= it } ?: true
            val matchesMax = filters.priceMax?.let { price <= it } ?: true
            val hasImage = if (filters.withImageOnly) ad.imageUrls.isNotEmpty() else true

            matchesMin && matchesMax && hasImage
        }

        _ads.value = filtered
    }
}
