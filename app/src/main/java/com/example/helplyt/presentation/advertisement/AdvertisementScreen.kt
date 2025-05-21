import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementScreen(
    navController: NavController,
    viewModel: AdvertisementViewModel = viewModel()
) {
    val ads by viewModel.ads.collectAsState()

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
    )
    { innerPadding ->

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
                    onClick = { /* TODO: obsługa filtrowania */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Filtry")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filtry")
                }

                OutlinedButton(
                    onClick = { /* TODO: obsługa sortowania */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Sortowanie")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sortowanie")
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
                        navController.navigate("adDetails/${ad.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun AdItem(ad: Advertisement, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
            .padding(4.dp)
            .clickable { onClick() },
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
