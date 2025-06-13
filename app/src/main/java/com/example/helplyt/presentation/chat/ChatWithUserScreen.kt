package com.example.helplyt.presentation.chat

import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.helplyt.domain.model.Chat
import com.example.helplyt.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.example.helplyt.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWithUserScreen(
    navController: NavController,
    ownerId: String,
    adId: String
) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val context = LocalContext.current

    var chatId by remember { mutableStateOf("") }
    var adTitle by remember { mutableStateOf("Ogłoszenie") }
    var ownerName by remember { mutableStateOf("Użytkownik") }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var messageText by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }

    val cameraUri = remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            uploadImage(it, chatId, currentUserId, db, storage)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri.value != null) {
            uploadImage(cameraUri.value!!, chatId, currentUserId, db, storage)
        }
    }

    LaunchedEffect(true) {
        db.collection("ads").document(adId).get().addOnSuccessListener {
            adTitle = it.getString("title") ?: "Ogłoszenie"
        }
        db.collection("users").document(ownerId).get().addOnSuccessListener {
            ownerName = it.getString("username") ?: it.getString("email") ?: "Użytkownik"
        }

        val existing = db.collection("chats")
            .whereEqualTo("adId", adId)
            .whereArrayContains("userIds", currentUserId)
            .get().await()
            .firstOrNull { it.toObject(Chat::class.java).userIds.contains(ownerId) }

        chatId = existing?.id ?: db.collection("chats").add(
            Chat(
                userIds = listOf(currentUserId, ownerId),
                adId = adId,
                adTitle = adTitle,
                lastMessage = "",
                lastTimestamp = System.currentTimeMillis()
            )
        ).await().id

        db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { it.toObject(Message::class.java) } ?: emptyList()
                messages = list

                snap?.documents?.forEach { doc ->
                    val msg = doc.toObject(Message::class.java)
                    if (msg != null && !msg.seenBy.contains(currentUserId)) {
                        db.collection("chats").document(chatId)
                            .collection("messages").document(doc.id)
                            .update("seenBy", FieldValue.arrayUnion(currentUserId))
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "$ownerName — $adTitle",
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF2E7D32))
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = {
                    showSheet = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj media")
                }

                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Wpisz wiadomość...") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Button(onClick = {
                    if (messageText.isNotBlank()) {
                        val msg = Message(
                            senderId = currentUserId,
                            content = messageText,
                            timestamp = System.currentTimeMillis(),
                            seenBy = listOf(currentUserId)
                        )
                        db.collection("chats").document(chatId)
                            .collection("messages").add(msg)
                        db.collection("chats").document(chatId)
                            .update(
                                "lastMessage", msg.content,
                                "lastTimestamp", msg.timestamp,
                                "lastSenderId", currentUserId
                            )
                        messageText = ""
                    }
                }) {
                    Text("Wyślij")
                }
            }
        }
    ) { padding ->
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        var lastDate by remember { mutableStateOf("") }

        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(8.dp)
        ) {
            messages.forEach { msg ->
                val thisDate = dateFormat.format(Date(msg.timestamp))
                if (thisDate != lastDate) {
                    lastDate = thisDate
                    item {
                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    thisDate,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                val isMine = msg.senderId == currentUserId
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            color = if (isMine) Color(0xFF4CAF50) else Color(0xFF90A4AE),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                if (!msg.imageUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = msg.imageUrl,
                                        contentDescription = "Zdjęcie",
                                        placeholder = painterResource(R.drawable.ic_placeholder),
                                        error = painterResource(R.drawable.ic_broken_image),
                                        modifier = Modifier
                                            .widthIn(max = 240.dp)
                                            .padding(bottom = 4.dp)
                                    )
                                }
                                if (msg.content.isNotBlank()) {
                                    Text(msg.content, color = Color.White)
                                }
                                Text(
                                    "${timeFormat.format(Date(msg.timestamp))}${if (isMine && msg.seenBy.contains(ownerId)) " ✔" else ""}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Dodaj zdjęcie",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Button(
                    onClick = {
                        val file = File(
                            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "${UUID.randomUUID()}.jpg"
                        )
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        cameraUri.value = uri
                        cameraLauncher.launch(uri)
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zrób zdjęcie")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                        showSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wybierz z galerii")
                }
            }
        }
    }
}

private fun uploadImage(
    uri: Uri,
    chatId: String,
    currentUserId: String,
    db: FirebaseFirestore,
    storage: FirebaseStorage
) {
    val fileName = UUID.randomUUID().toString() + ".jpg"
    val fileRef = storage.reference.child("chat_images/$chatId/$fileName")

    fileRef.putFile(uri)
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val msg = Message(
                    senderId = currentUserId,
                    content = "",
                    timestamp = System.currentTimeMillis(),
                    imageUrl = downloadUri.toString(),
                    seenBy = listOf(currentUserId)
                )
                db.collection("chats").document(chatId)
                    .collection("messages").add(msg)
                db.collection("chats").document(chatId)
                    .update(
                        "lastMessage", "🖼️ zdjęcie",
                        "lastTimestamp", msg.timestamp,
                        "lastSenderId", currentUserId
                    )
            }
        }
        .addOnFailureListener {
            Log.e("uploadImage", "Błąd przy przesyłaniu zdjęcia", it)
        }

}
