import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.use_case.LoginUseCase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    var loginState by mutableStateOf<Result<FirebaseUser?>?>(null)
        private set

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loginState = loginUseCase(email, password)
        }
    }
    fun resetLoginState() {
        loginState = null
    }

}