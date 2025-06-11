import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.app.data.UserPreferences
import com.example.app.utils.ValidationUtils.isEmailValid
import com.example.helplyt.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.example.app.navigation.Screen

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navController: NavHostController,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    // Google SignIn setup
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.loginWithGoogle(idToken)
            } else {
                Toast.makeText(context, "Token logowania Google jest null", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google logowanie nieudane: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Wyloguj Google i Firebase przed sprawdzeniem czy użytkownik jest zalogowany
    LaunchedEffect(Unit) {
        googleSignInClient.signOut()
        FirebaseAuth.getInstance().signOut()

        val (savedEmail, savedPassword, savedRememberMe) = userPrefs.loadCredentials()
        if (savedRememberMe && savedEmail != null && savedPassword != null) {
            email = savedEmail
            password = savedPassword
            viewModel.login(savedEmail, savedPassword)
        } else {
            viewModel.checkIfUserLoggedIn()
        }
    }

    // Obserwuj stany z ViewModel
    val loginResult by remember { derivedStateOf { viewModel.loginState } }
    val needsSetup by remember { derivedStateOf { viewModel.needsProfileSetup } }

    // Reaguj na zmiany loginResult i needsSetup, wykonaj nawigację i reset stanu
    LaunchedEffect(loginResult, needsSetup) {
        val result = loginResult // przypisz do lokalnej zmiennej

        if (result != null) {
            if (result.isSuccess) {
                Toast.makeText(context, "Zalogowano pomyślnie", Toast.LENGTH_LONG).show()
                if (needsSetup) {
                    navController.navigate(Screen.SetupProfile.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                viewModel.resetLoginState()
            } else if (result.isFailure) {
                Toast.makeText(context, result.exceptionOrNull()?.message ?: "Błąd logowania", Toast.LENGTH_LONG).show()
                viewModel.resetLoginState()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_logo),
            contentDescription = null,
            modifier = Modifier
                .size(325.dp)
                .padding(bottom = 24.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp)
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Zapamiętaj mnie",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Card(
            onClick = {
                if (!isEmailValid(email)) {
                    Toast.makeText(context, "Nieprawidłowy adres email", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.login(email, password)
                    coroutineScope.launch {
                        if (rememberMe) {
                            userPrefs.saveCredentials(email, password)
                        } else {
                            userPrefs.clearCredentials()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Zaloguj się",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Email, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Zaloguj się przez Google")
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Nie masz konta? Zarejestruj się")
        }
    }
}
