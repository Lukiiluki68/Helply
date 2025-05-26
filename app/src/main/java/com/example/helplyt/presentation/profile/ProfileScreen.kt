
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.helplyt.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.app.data.UserPreferences
import androidx.navigation.NavController

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }

    val avatarUri by viewModel.avatarUri.collectAsState()
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) viewModel.setAvatar(uri) }

    var showDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = if (avatarUri != null)
                    rememberAsyncImagePainter(avatarUri)
                else painterResource(id = R.drawable.avatar_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Zmień zdjęcie", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(username, style = MaterialTheme.typography.headlineSmall)
        Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))

        ProfileOption(icon = Icons.Default.Person, label = "Twoje dane") {
            showDataDialog = true
            // TODO: Połączenie z Firebase
        }

        ProfileOption(icon = Icons.Default.Lock, label = "Hasło") {
            // TODO: Zmiana hasła
        }

        ProfileOption(icon = Icons.Default.Home, label = "Adres") {
            // TODO: Pokaz adres z ogłoszenia
        }

        ProfileOption(icon = Icons.Default.ShoppingCart, label = "Metody płatności") {
            // TODO: Metody płatności
        }

        ProfileOption(icon = Icons.Default.Notifications, label = "Powiadomienia") {
            // TODO: Powiadomienia
        }

        ProfileOption(
            icon = Icons.Default.Delete,
            label = "Usuń konto",
            containerColor = Color.Red,
            textColor = Color.White
        ) {
            // TODO: Usuń konto
        }

        ProfileOption(icon = Icons.Default.ExitToApp, label = "Wyloguj się") {
            FirebaseAuth.getInstance().signOut()
            scope.launch {
                userPrefs.clearCredentials()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }

    if (showDataDialog) {
        AlertDialog(
            onDismissRequest = { showDataDialog = false },
            confirmButton = {
                TextButton(onClick = { showDataDialog = false }) {
                    Text("Zamknij")
                }
            },
            title = { Text("Twoje dane") },
            text = {
                Column {
                    Text("Imię: Jan")
                    Text("Nazwisko: Kowalski")
                    Text("Email: $email")
                }
            }
        )
    }
}

@Composable
fun ProfileOption(
    icon: ImageVector,
    label: String,
    containerColor: Color = Color.White,
    textColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = textColor
        )
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.padding(end = 8.dp))
        Text(label)
    }
}
