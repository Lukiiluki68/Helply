package com.example.app.presentation.home

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.app.data.UserPreferences
import com.example.helplyt.R
import com.example.helplyt.presentation.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    onCreateAdClick: () -> Unit = {},
    onBrowseAdsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val userPreferences = remember { UserPreferences(context) }
        val fadeIn by animateFloatAsState(targetValue = 1f, animationSpec = tween(1000))
        val isDark = isSystemInDarkTheme()
        val logoRes = if (isDark) R.drawable.text_logo_dark else R.drawable.text_logo
        val profile by profileViewModel.profile.collectAsState()


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {


            IconButton(
                onClick = {
                    navController.navigate("profile")
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 2.dp, end = 2.dp)
                    .size(45.dp)

            ) {
                Image(
                    painter = if (profile.avatarUrl != null)
                        rememberAsyncImagePainter(profile.avatarUrl)
                    else painterResource(id = R.drawable.avatar_placeholder),
                    contentDescription = "Profil",
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 24.dp, end = 24.dp)
                .alpha(fadeIn),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null,
                modifier = Modifier.size(350.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    onClick = { navController.navigate("createAd") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Stwórz ogłoszenie",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    onClick = { navController.navigate("Advertisement") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Przeglądaj ogłoszenia",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    onClick = { navController.navigate("myAdvertisements") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Moje ogłoszenia",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    onClick = { navController.navigate("ChatList") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD600))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Chat",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(400.dp))
        }
    }
}