import android.net.Uri
import com.example.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UploadAvatarUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uri: Uri): Uri? {
        return userRepository.uploadAvatar(uri)
    }
}
