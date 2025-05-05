import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.app.presentation.register.RegisterViewModel
import com.example.app.utils.ValidationUtils.isEmailValid
import com.example.app.utils.ValidationUtils.isPasswordValid

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            when {
                !isEmailValid(email) -> {
                    Toast.makeText(context, "Nieprawidłowy adres email", Toast.LENGTH_LONG).show()
                }
                !isPasswordValid(password) -> {
                    Toast.makeText(
                        context,
                        "Hasło musi mieć min. 8 znaków, 1 dużą literę, 1 cyfrę i 1 znak specjalny",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    viewModel.register(email, password) {
                        if (it.isSuccess) {
                            Toast.makeText(context, "Rejestracja zakończona sukcesem", Toast.LENGTH_LONG).show()
                            onRegisterSuccess()
                        } else {
                            Toast.makeText(context, it.exceptionOrNull()?.message ?: "Błąd", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }) {
            Text("Zarejestruj się")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("Masz już konto? Zaloguj się")
        }
    }
}
