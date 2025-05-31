
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
import com.example.helplyt.presentation.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }

    val profile by viewModel.profile.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) viewModel.setAvatar(uri) }

    var showDataDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(34.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = if (profile.avatarUrl != null)
                    rememberAsyncImagePainter(profile.avatarUrl)
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
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Zmień zdjęcie",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(34.dp))

        ProfileOption(
            icon = Icons.Default.Person,
            label = "Twoje dane",
            onClick = { showDataDialog = true }
        )

        ProfileOption(
            icon = Icons.Default.Lock,
            label = "Hasło",
            onClick = {
                navController.navigate("changePassword")
            }
        )


        ProfileOption(
            icon = Icons.Default.Home,
            label = "Adres",
            onClick = {
                navController.navigate("changeAddress")
            }
        )

        ProfileOption(
            icon = Icons.Default.ShoppingCart,
            label = "Metody płatności",
            onClick = { showPaymentDialog = true }
        )

        ProfileOption(
            icon = Icons.Default.Notifications,
            label = "Powiadomienia",
            onClick = { showNotificationDialog = true }
        )

        ProfileOption(
            icon = Icons.Default.Delete,
            label = "Usuń konto",
            containerColor = Color.Red,
            contentColor = Color.White,
            onClick = { showDeleteDialog = true }
        )

        ProfileOption(
            icon = Icons.Default.ExitToApp,
            label = "Wyloguj się",
            onClick = {
                FirebaseAuth.getInstance().signOut()
                scope.launch {
                    userPrefs.clearCredentials()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
        )
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
                    Text("Imię i nazwisko: ${profile.username}")
                    Text("Email: ${profile.email}")
                    Text("Data urodzenia: ${profile.birthDate}")
                }
            }
        )
    }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            confirmButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Zamknij")
                }
            },
            title = { Text("Metody płatności") },
            text = { Text("Opcja rozwoju aplikacji w system płatności") }
        )
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Zamknij")
                }
            },
            title = { Text("Powiadomienia") },
            text = { Text("Tutaj będzie można zarządzać powiadomieniami – TODO") }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Anuluj")
                }
            },
            title = { Text("Usuń konto") },
            text = { Text("Tu dodamy potwierdzenie i logikę usunięcia konta – TODO") }
        )
    }
}

@Composable
fun ProfileOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}