package com.example.helplyt.presentation.my_advertisement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.helplyt.model.model.Opinion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOpinionScreen(userId: String, navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var userName by remember { mutableStateOf("Ja") }
    val opinions = remember { mutableStateListOf<Opinion>() }

    LaunchedEffect(currentUserId) {
        if (currentUserId == null) return@LaunchedEffect

        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                userName = doc.getString("username") ?: "Ja"
            }

        firestore.collection("opinions")
            .whereEqualTo("recipientUserId", currentUserId)
            .get()
            .addOnSuccessListener { result ->
                val fetchedOpinions = result.documents.mapNotNull { it.toObject(Opinion::class.java) }
                opinions.clear()
                opinions.addAll(fetchedOpinions)

                fetchedOpinions.forEachIndexed { index, opinion ->
                    firestore.collection("users").document(opinion.authorUserId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val username = userDoc.getString("username") ?: "Nieznany"
                            opinions[index] = opinion.copy(authorName = username)
                        }
                }
            }
            .addOnFailureListener {
                println("Błąd pobierania opinii: ${it.message}")
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opinie o mnie", style = MaterialTheme.typography.titleLarge, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cofnij", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(text = userName, style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            if (opinions.isEmpty()) {
                Text("Nie otrzymałeś jeszcze żadnych opinii.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                opinions.forEach {
                    DetailedOpinionItem(opinion = it)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DetailedOpinionItem(opinion: Opinion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val formattedRating = if (opinion.rating % 1.0 == 0.0)
                opinion.rating.toInt().toString()
            else
                opinion.rating.toString()

            Text(text = "⭐ Ocena: $formattedRating/5", fontWeight = FontWeight.Bold)
            Text(text = opinion.content)
            Text(text = "Autor: ${opinion.authorName}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Data: ${opinion.date}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
