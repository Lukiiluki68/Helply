import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.use_case.LoginUseCase
import com.example.app.domain.repository.UserRepository // dodaj repozytorium do profili
import com.example.helplyt.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    var loginState by mutableStateOf<Result<FirebaseUser?>?>(null)
        private set

    // Nowy stan, czy użytkownik musi uzupełnić profil
    var needsProfileSetup by mutableStateOf(false)
        private set

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loginState = loginUseCase(email, password)
            if (loginState?.isSuccess == true) {
                checkUserProfile()
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                loginState = Result.success(result.user)
                checkUserProfile()
            } catch (e: Exception) {
                loginState = Result.failure(e)
            }
        }
    }
    fun checkIfUserLoggedIn() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            loginState = Result.success(currentUser)
            viewModelScope.launch {
                checkUserProfile()
            }
        }
    }


    private suspend fun checkUserProfile() {
        val profile = userRepository.getUserProfile()
        // Sprawdź, czy profil jest uzupełniony — np. username i birthDate nie są puste
        needsProfileSetup = profile == null || profile.username.isBlank() || profile.birthDate.isBlank()
        println("Profile check: $profile, needsSetup=$needsProfileSetup")
    }

    fun resetLoginState() {
        loginState = null
        needsProfileSetup = false
    }
}
