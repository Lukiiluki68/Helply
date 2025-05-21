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
        imageUri: Uri?,
        context: Context
    ) {
        if (imageUri != null) {
            val imageRef = storage.child("ads_images/${UUID.randomUUID()}")
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveAdToFirestore(title, description, price, date, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Błąd przesyłania zdjęcia", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveAdToFirestore(title, description, price, date, null)
        }
    }

    private fun saveAdToFirestore(
        title: String,
        description: String,
        price: String,
        date: String,
        imageUrl: String?
    ) {
        val ad = hashMapOf(
            "title" to title,
            "description" to description,
            "price" to price,
            "executionDate" to date,
            "imageUrl" to imageUrl,
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
