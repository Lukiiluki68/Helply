import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.app.navigation.Screen
import com.example.helplyt.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementScreen(
    navController: NavController,
    viewModel: AdvertisementViewModel = viewModel()
) {
    val ads by viewModel.ads.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0f..1000f) }
    var onlyWithImage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ogłoszenia") },
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Filtry")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filtry")
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Sortowanie")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (sortOrder) {
                                SortOrder.NEWEST -> "Najnowsze"
                                SortOrder.OLDEST -> "Najstarsze"
                                SortOrder.PRICE_ASC -> "Cena ↑"
                                SortOrder.PRICE_DESC -> "Cena ↓"
                            }
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Najnowsze") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NEWEST)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Najstarsze") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.OLDEST)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cena rosnąco") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.PRICE_ASC)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cena malejąco") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.PRICE_DESC)
                                expanded = false
                            }
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ads) { ad ->
                    AdItem(ad = ad, onClick = {
                        navController.navigate(Screen.AdDetails.createRoute(ad.id))
                    })
                }
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setFilter(
                        sliderPosition.start.toInt(),
                        sliderPosition.endInclusive.toInt(),
                        onlyWithImage
                    )
                    showFilterDialog = false
                }) {
                    Text("Zastosuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Anuluj")
                }
            },
            title = { Text("Filtruj ogłoszenia") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Zakres ceny", style = MaterialTheme.typography.bodyMedium)

                    RangeSlider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..1000f,
                        steps = 9
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Od ${sliderPosition.start.toInt()} zł")
                        Text("Do ${sliderPosition.endInclusive.toInt()} zł")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = onlyWithImage,
                            onCheckedChange = { onlyWithImage = it }
                        )
                        Text("Tylko ze zdjęciem")
                    }
                }
            }
        )
    }
}

@Composable
fun AdItem(ad: Advertisement, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 6.dp,
        color = Color.White
    ) {
        Column {
            val painter = rememberAsyncImagePainter(
                model = ad.mainImageUrl,
                fallback = painterResource(R.drawable.no_image),
                error = painterResource(R.drawable.no_image),
                placeholder = painterResource(R.drawable.no_image)
            )

            Image(
                painter = painter,
                contentDescription = "Zdjęcie ogłoszenia",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = ad.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${ad.price} zł",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}