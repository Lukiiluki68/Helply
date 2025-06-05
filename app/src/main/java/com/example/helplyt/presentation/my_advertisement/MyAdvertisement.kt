package com.example.helplyt.presentation.my_advertisement

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAdvertisementScreen(
    navController: NavController,
    viewModel: MyAdvertisementViewModel = viewModel()
) {
    val ads by viewModel.myAds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moje ogłoszenia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
        ) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ads) { ad ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                            .padding(4.dp)
                            .clickable {
                                navController.navigate("adDetails/${ad.id}")
                            },
                        color = Color.White,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ad.imageUrl?.let { url ->
                                Image(
                                    painter = rememberAsyncImagePainter(url),
                                    contentDescription = "Zdjęcie ogłoszenia",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(text = ad.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = ad.price, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
