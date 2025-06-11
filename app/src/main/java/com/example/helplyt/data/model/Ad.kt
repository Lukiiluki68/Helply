data class Ad(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val executionDate: String = "",
    val imageUrls: List<String> = emptyList(),
    val location: String = "",
    val userId: String = "",
    val acceptedUserId: String? = null,
    val timestamp: Long = 0,
    val imageUrl: String? = null,
    val applicantUserIds: List<String> = emptyList()
) {
    val mainImageUrl: String?
        get() = imageUrls.firstOrNull()
}
