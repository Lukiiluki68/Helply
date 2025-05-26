
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val _username = MutableStateFlow("Jan Kowalski")
    val username: StateFlow<String> = _username

    private val _email = MutableStateFlow("jan.kowalski@example.com")
    val email: StateFlow<String> = _email

    private val _avatarUri = MutableStateFlow<Uri?>(null)
    val avatarUri: StateFlow<Uri?> = _avatarUri

    fun setAvatar(uri: Uri) {
        _avatarUri.value = uri
    }

    // Możesz dodać funkcje edycji danych, wylogowania itp.
}
