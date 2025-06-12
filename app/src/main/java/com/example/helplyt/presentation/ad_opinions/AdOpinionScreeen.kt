package com.example.helplyt.presentation.user_opinions

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun AddOpinionScreen(
    navController: NavController,
    recipientUserId: String
) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var rating by remember { mutableStateOf(1) }
    var content by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Logi do debugowania wartości rating i content
    LaunchedEffect(rating) { println("Aktualna wartość rating: $rating") }
    LaunchedEffect(content) { println("Aktualna wartość content: $content") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj opinię") },
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Treść opinii") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 5
            )

            Text("Ocena:", style = MaterialTheme.typography.bodyMedium)
            StarRating(rating = rating, onRatingChanged = { rating = it })

            Button(
                onClick = {
                    println("Kliknięcie przycisku: rating=$rating, content='$content', currentUser=${currentUser?.uid}")

                    if (rating in 1..5 && content.isNotBlank() && currentUser != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val opinion = mapOf(
                            "content" to content,
                            "rating" to rating,
                            "date" to sdf.format(Date()),
                            "authorUserId" to currentUser.uid,
                            "recipientUserId" to recipientUserId
                        )

                        db.collection("opinions")
                            .add(opinion)
                            .addOnSuccessListener {
                                println("Opinia dodana pomyślnie!")
                                showSuccess = true
                            }
                            .addOnFailureListener {
                                println("Błąd dodawania opinii: ${it.message}")
                                showError = true
                            }
                    } else {
                        println("BŁĄD: Niepoprawne dane - rating=$rating, content='$content'")
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Dodaj opinię")
            }

            if (showError) {
                Text(
                    text = "Uzupełnij poprawnie treść i ocenę (1–5).",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (showSuccess) {
                AlertDialog(
                    onDismissRequest = {
                        showSuccess = false
                        navController.popBackStack()
                    },
                    title = { Text("Sukces") },
                    text = { Text("Opinia została dodana.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showSuccess = false
                            navController.popBackStack()
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StarRating(
    rating: Int,
    onRatingChanged: (Int) -> Unit
) {
    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.Center) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = Color.Yellow,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChanged(i) }
                    .padding(4.dp)
            )
        }
    }
}
