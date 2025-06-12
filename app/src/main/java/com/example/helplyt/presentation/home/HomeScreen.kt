package com.example.app.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.app.data.UserPreferences
import com.example.app.navigation.Screen
import com.example.helplyt.R
import com.example.helplyt.presentation.profile.ProfileViewModel

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
                .padding(top = 72.dp, start = 24.dp, end = 24.dp, bottom = 32.dp)
                .alpha(fadeIn),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                ActionButton(
                    text = "Stwórz ogłoszenie",
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate("createAd") },
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

                ActionButton(
                    text = "Przeglądaj ogłoszenia",
                    icon = Icons.Default.List,
                    onClick = { navController.navigate("Advertisement") },
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

                ActionButton(
                    text = "Moje ogłoszenia",
                    icon = Icons.Default.List,
                    onClick = { navController.navigate("myAdvertisements") },
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

                ActionButton(
                    text = "Czat",
                    icon = Icons.Default.Chat,
                    onClick = { navController.navigate(Screen.ChatList.route) },
                    backgroundColor = Color(0xFFFFD600), // żółty
                    contentColor = Color.White
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color
) {
    Spacer(modifier = Modifier.height(12.dp))
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = text,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
