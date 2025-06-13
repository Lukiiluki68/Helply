package com.example.helplyt.presentation.chat


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.helplyt.domain.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("HH:mm dd.MM", Locale.getDefault())
    return sdf.format(Date(timestamp))
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    var chatList by remember { mutableStateOf<List<Pair<Chat, Int>>>(emptyList()) }

    LaunchedEffect(true) {
        val snapshot = db.collection("chats")
            .whereArrayContains("userIds", currentUserId)
            .get()
            .await()

        val chats = snapshot.documents.mapNotNull { doc ->
            val chat = doc.toObject(Chat::class.java)?.copy(id = doc.id)
            chat
        }

        val sortedChats = chats.sortedByDescending { it.lastTimestamp }

        val updatedList = sortedChats.map { chat ->
            val messagesSnapshot = db.collection("chats")
                .document(chat.id)
                .collection("messages")
                .get()
                .await()

            val unreadCount = messagesSnapshot.documents.count { doc ->
                val seenBy = doc.get("seenBy") as? List<*> ?: emptyList<Any>()
                !seenBy.contains(currentUserId)
            }

            chat to unreadCount
        }

        chatList = updatedList
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
            items(chatList) { (chat, unreadCount) ->
                val otherUserId = chat.userIds.firstOrNull { it != currentUserId } ?: return@items
                var userName by remember { mutableStateOf("Użytkownik") }
                var showDialog by remember { mutableStateOf(false) }
                var performChatDelete by remember { mutableStateOf(false) }

                LaunchedEffect(otherUserId) {
                    val userSnap = db.collection("users").document(otherUserId).get().await()
                    userName = userSnap.getString("username") ?: userSnap.getString("email") ?: "Użytkownik"
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Usuń rozmowę") },
                        text = { Text("Czy na pewno chcesz usunąć rozmowę z $userName?") },
                        confirmButton = {
                            TextButton(onClick = {
                                performChatDelete = true
                                showDialog = false
                            }) {
                                Text("Tak")
                            }
                        }
                        ,
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Nie")
                            }
                        }
                    )
                    LaunchedEffect(performChatDelete) {
                        if (performChatDelete) {
                            deleteFullChat(chat.id, db)
                            chatList = chatList.filterNot { it.first.id == chat.id }
                            performChatDelete = false
                        }
                    }

                }
                LimitedSwipeItem(
                    onSwipeConfirm = { showDialog = true }
                ) {
                    ChatListItem(
                        chat = chat,
                        userName = userName,
                        unreadCount = unreadCount,
                        onClick = {
                            navController.navigate("chatWith/$otherUserId/${chat.adId}")
                        },
                        onLongPressConfirmed = { showDialog = true }
                    )
                }

            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    chat: Chat,
    userName: String,
    unreadCount: Int,
    onClick: () -> Unit,
    onLongPressConfirmed: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Usuń rozmowę") },
            text = { Text("Czy na pewno chcesz usunąć rozmowę z $userName?") },
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
        colors = CardDefaults.cardColors(containerColor = if (unreadCount > 0) Color(0xFFE3F2FD) else Color(0xFFF1F1F1))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${chat.adTitle} — $userName",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTimestamp(chat.lastTimestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = chat.lastMessage.ifBlank { "Brak wiadomości" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (unreadCount > 0) {
                    Text(
                        text = unreadCount.toString(),
                        color = Color.White,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(Color(0xFF1565C0), shape = MaterialTheme.shapes.medium)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun LimitedSwipeItem(
    modifier: Modifier = Modifier,
    onSwipeConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    val maxOffsetX = -100f
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "swipe")

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX < maxOffsetX * 0.5f) {
                            offsetX = maxOffsetX
                            onSwipeConfirm()
                        } else {
                            offsetX = 0f // wróć
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(maxOffsetX, 0f)
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFFFCDD2), shape = MaterialTheme.shapes.medium),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Usuń",
                tint = Color.Red,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
        ) {
            content()
        }
    }
}

suspend fun deleteFullChat(chatId: String, db: FirebaseFirestore) {
    val storage = FirebaseStorage.getInstance()

    // Usuń wiadomości
    val messages = db.collection("chats").document(chatId).collection("messages").get().await()
    for (msg in messages) {
        msg.reference.delete()
    }

    // Usuń zdjęcia z Storage
    val imageRefs = storage.reference.child("chat_images/$chatId").listAll().await()
    for (item in imageRefs.items) {
        item.delete().await()
    }

    // Usuń dokument czatu
    db.collection("chats").document(chatId).delete()
}