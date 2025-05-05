import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.app.data.UserPreferences
import com.example.app.utils.ValidationUtils.isEmailValid
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    // Automatyczne wczytanie zapisanych danych przy starcie ekranu
    LaunchedEffect(Unit) {
        val (savedEmail, savedPassword, savedRememberMe) = userPrefs.loadCredentials()
        if (savedRememberMe && savedEmail != null && savedPassword != null) {
            email = savedEmail
            password = savedPassword
            viewModel.login(savedEmail, savedPassword)
        }
    }

    val loginResult = viewModel.loginState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it }
            )
            Text("Zapamiętaj mnie")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (!isEmailValid(email)) {
                Toast.makeText(context, "Nieprawidłowy adres email", Toast.LENGTH_LONG).show()
            } else {
                viewModel.login(email, password)
                if (rememberMe) {
                    coroutineScope.launch {
                        userPrefs.saveCredentials(email, password)
                    }
                } else {
                    coroutineScope.launch {
                        userPrefs.clearCredentials()
                    }
                }
            }
        }) {
            Text("Zaloguj się")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("Nie masz konta? Zarejestruj się")
        }

        loginResult?.let {
            if (it.isSuccess) {
                Toast.makeText(context, "Zalogowano pomyślnie", Toast.LENGTH_LONG).show()
                viewModel.resetLoginState()
                onLoginSuccess()
            } else {
                Toast.makeText(context, it.exceptionOrNull()?.message ?: "Błąd logowania", Toast.LENGTH_LONG).show()
            }
        }
    }
}
