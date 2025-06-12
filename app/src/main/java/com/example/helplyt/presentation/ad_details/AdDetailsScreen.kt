package com.example.helplyt.presentation.ad_details

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.helplyt.presentation.common.MapSinglePinView
import com.example.helplyt.presentation.my_advertisement.MyAdvertisementViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun AdDetailsScreen(
    navController: NavController,
    adId: String
) {
    val viewModel: AdDetailsViewModel = viewModel(factory = AdDetailsViewModelFactory(adId))
    val adData by viewModel.adData.collectAsState()
    val userData by viewModel.userData.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    val isOwner = adData?.userId == currentUserId
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnacceptDialog by remember { mutableStateOf(false) }
    val myAdViewModel: MyAdvertisementViewModel = viewModel()
    val applicantUsers by viewModel.applicantUsers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły ogłoszenia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cofnij")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 📢 Ogłoszenie od
            val displayName = userData?.username?.ifBlank { userData?.email } ?: "Nieznany użytkownik"
            val userId = adData?.userId ?: "Nieznany użytkownik"

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clickable {
                        navController.navigate("userProfile/$userId") // przejście do profilu z opiniami
                    }
            ) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "📢 Ogłoszenie od: $displayName",
                        modifier = Modifier.padding(8.dp),
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 🏷 Tytuł ogłoszenia
            Text(
                text = adData?.title ?: "...",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            // 📝 Opis
            adData?.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 🖼 Zdjęcia
            val imageUrls = adData?.imageUrls ?: emptyList()
            if (imageUrls.isNotEmpty()) {
                val pagerState = rememberPagerState()

                Column {
                    HorizontalPager(
                        state = pagerState,
                        count = imageUrls.size,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) { page ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxSize()
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUrls[page]),
                                contentDescription = "Zdjęcie ogłoszenia",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    )
                }
            }

            // 🗓 Termin wykonania (niebieskie tło)
            adData?.executionDate?.takeIf { it.isNotBlank() }?.let { date ->
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "📅 Termin wykonania: $date",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        color = Color(0xFF1565C0)
                    )
                }
            }

            // 📍 Lokalizacja + mapa
            adData?.location?.takeIf { it.isNotBlank() }?.let { location ->
                Surface(
                    color = Color(0xFFF1F8E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "📍 Lokalizacja: $location",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                MapSinglePinView(address = location)
            }

            // 💰 Cena (zielony box)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "💰 Cena: ${adData?.price ?: "0"} zł",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = Color(0xFF388E3C),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // ✅ Przycisk AKCEPTUJ na dole (przewijalny)
            if (isOwner) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            navController.navigate("editAd/${adId}")
                        },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Edytuj")
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Usuń")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                val selectedUser = applicantUsers.find { it.userId == adData?.acceptedUserId }

                if (selectedUser != null) {
                    Text(
                        text = "✅ Wybrany użytkownik: ${selectedUser.username ?: selectedUser.email}",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (applicantUsers.isNotEmpty() && adData?.acceptedUserId == null) {
                    Text(
                        text = "Zgłoszeni użytkownicy:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    applicantUsers.forEach { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = user.username ?: user.email ?: "Użytkownik",
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            navController.navigate("userProfile/${user.userId}")
                                        },
                                    fontWeight = FontWeight.Medium
                                )
                                Button(onClick = {
                                    viewModel.acceptUserForAd(adId, user.userId)
                                    viewModel.reload()
                                }) {
                                    Text("Wybierz")
                                }
                            }
                        }
                    }
                }

            } else {
                val hasApplied = adData?.applicantUserIds?.contains(currentUserId) == true


                when {
                    !hasApplied -> {
                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Zgłoś się")
                        }
                    }

                    hasApplied -> {
                        Button(
                            onClick = { /* brak akcji */ },
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray,
                                disabledContainerColor = Color.LightGray,
                                disabledContentColor = Color.DarkGray
                            )
                        ) {
                            Text("Zgłoszono się")
                        }
                    }

                }
            }
        }

        // 💬 Dialog potwierdzający
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Potwierdzenie") },
                text = { Text("Czy na pewno chcesz zgłosić się do tego ogłoszenia?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                // dodanie zgłoszenia do listy applicants
                                FirebaseFirestore.getInstance()
                                    .collection("ads")
                                    .document(adId)
                                    .update("applicantUserIds", FieldValue.arrayUnion(currentUserId))
                                    .addOnSuccessListener {
                                        // np. pokazanie potwierdzenia lub przejście dalej
                                        navController.popBackStack()
                                    }
                            }
                        }
                    ) {
                        Text("Tak", color = Color(0xFF4CAF50))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Nie", color = Color(0xFFF44336))
                    }
                },
                shape = RoundedCornerShape(12.dp),
                containerColor = Color.White
            )
        }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Potwierdź usunięcie") },
            text = { Text("Czy na pewno chcesz usunąć to ogłoszenie?") },
            confirmButton = {
                TextButton(onClick = {
                    val imageUrls = adData?.imageUrls ?: emptyList()
                    viewModel.deleteAdWithImages(adId, imageUrls) {
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                }) {
                    Text("Tak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
    if (showUnacceptDialog) {
        AlertDialog(
            onDismissRequest = { showUnacceptDialog = false },
            title = { Text("Potwierdzenie") },
            text = { Text("Czy na pewno chcesz zrezygnować z tego ogłoszenia?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnacceptDialog = false
                    FirebaseFirestore.getInstance()
                        .collection("ads")
                        .document(adId)
                        .update("acceptedUserId", null)
                        .addOnSuccessListener {
                            myAdViewModel.reload()
                            navController.popBackStack()
                        }
                }) {
                    Text("Tak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnacceptDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

}
}
