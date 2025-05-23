import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
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
        location: String = "" // tymczasowo puste
    ) {
        if (imageUris.isEmpty()) {
            saveAdToFirestore(title, description, price, date, emptyList())
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
                            saveAdToFirestore(title, description, price, date, imageUrls)
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
        imageUrls: List<String>

    ) {
        val ad = hashMapOf(
            "title" to title,
            "description" to description,
            "price" to price,
            "executionDate" to date,
            "imageUrls" to imageUrls,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("ads")
            .add(ad)
            .addOnSuccessListener {
                Log.d("CreateAd", "Ogłoszenie zapisane: ${it.id}")
            }
            .addOnFailureListener {
                Log.e("CreateAd", "Błąd zapisu ogłoszenia", it)
            }
    }
}
