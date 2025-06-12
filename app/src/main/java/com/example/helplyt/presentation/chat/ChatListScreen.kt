package com.example.helplyt.presentation.chat


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.helplyt.domain.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var chatList by remember { mutableStateOf<List<Chat>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(true) {
        val snapshot = db.collection("chats")
            .whereArrayContains("userIds", currentUserId)
            .get()
            .await()

        val chats = snapshot.documents.mapNotNull { doc ->
            val chat = doc.toObject(Chat::class.java)?.copy(id = doc.id)
            chat
        }

        chatList = chats.sortedByDescending { it.lastTimestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Czaty") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(chatList) { chat ->
                val otherUserId = chat.userIds.firstOrNull { it != currentUserId } ?: return@items
                var userName by remember { mutableStateOf("Użytkownik") }

                LaunchedEffect(otherUserId) {
                    val userSnap = db.collection("users").document(otherUserId).get().await()
                    userName = userSnap.getString("username") ?: userSnap.getString("email") ?: "Użytkownik"
                }

                ChatListItem(
                    chat = chat,
                    userName = userName,
                    onClick = {
                        navController.navigate("chatWith/$otherUserId/${chat.adId}")
                    },
                    onLongPressConfirmed = {
                        db.collection("chats").document(chat.id).delete()
                        chatList = chatList.filterNot { it.id == chat.id }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    chat: Chat,
    userName: String,
    onClick: () -> Unit,
    onLongPressConfirmed: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Usuń czat") },
            text = { Text("Czy na pewno chcesz usunąć ten czat?") },
            confirmButton = {
                TextButton(onClick = {
                    onLongPressConfirmed()
                    showDialog = false
                }) {
                    Text("Tak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Nie")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDialog = true }
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${chat.adTitle} — $userName",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage.ifBlank { "🖼️ zdjęcie" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}
