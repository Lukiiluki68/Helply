
import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdScreen(
    navController: NavController,
    viewModel: CreateAdViewModel = viewModel(),
    onBack: () -> Unit = { navController.popBackStack() }
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf(false) }
    val imageUris = remember { mutableStateListOf<Uri>() }

    var showLocationDialog by remember { mutableStateOf(false) }
    var zip by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var useDefaultAddress by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            date = "%02d-%02d-%04d".format(day, month + 1, year)
            dateError = false
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val remainingSlots = 5 - imageUris.size
        val selected = uris.take(remainingSlots)
        imageUris.addAll(selected)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj ogłoszenie") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageUris) { uri ->
                    Box(modifier = Modifier.size(160.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(Color.White, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { imageUris.remove(uri) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Usuń",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }

                if (imageUris.size < 5) {
                    item {
                        OutlinedButton(
                            onClick = { launcher.launch(arrayOf("image/*")) },
                            modifier = Modifier.size(160.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Dodaj")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tytuł") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = price,
                onValueChange = { input ->
                    // Filtruj tylko cyfry i ewentualnie przecinek/kropkę
                    if (input.matches(Regex("^\\d*([.,]\\d{0,2})?$"))) {
                        price = input
                    }
                },
                label = { Text("Cena") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = date,
                onValueChange = { input ->
                    date = input
                    dateError = true

                    val parts = input.split("-")
                    if (parts.size == 3 && parts[0].length == 2 && parts[1].length == 2 && parts[2].length == 4) {
                        try {
                            val day = parts[0].toInt()
                            val month = parts[1].toInt()
                            val year = parts[2].toInt()

                            val inputCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month - 1)
                                set(Calendar.DAY_OF_MONTH, day)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val now = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val valid = day == inputCal.get(Calendar.DAY_OF_MONTH) &&
                                    month - 1 == inputCal.get(Calendar.MONTH) &&
                                    year == inputCal.get(Calendar.YEAR)

                            dateError = !(valid && !inputCal.before(now))
                        } catch (e: Exception) {
                            dateError = true
                        }
                    }
                },
                label = { Text("Data wykonania") },
                placeholder = { Text("dd-mm-rrrr") },
                isError = dateError,
                supportingText = {
                    if (dateError) Text("Wpisz poprawną przyszłą datę w formacie dd-mm-rrrr", color = MaterialTheme.colorScheme.error)
                },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Kalendarz")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )


            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showLocationDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dodaj lokalizację")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                onClick = {
                    val fullAddress = "$street $building, $zip $city"
                    viewModel.createAd(title, description, price, date, imageUris.toList(), context, fullAddress)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Dodaj ogłoszenie",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            confirmButton = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useDefaultAddress,
                            onCheckedChange = {
                                useDefaultAddress = it
                                if (it) {
                                    viewModel.loadUserAddress { profile ->
                                        zip = profile.postalCode
                                        city = profile.city
                                        street = profile.street
                                        building = profile.number
                                    }
                                } else {
                                    zip = ""
                                    city = ""
                                    street = ""
                                    building = ""
                                }
                            }
                        )
                        Text("Użyj domyślnego adresu")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showLocationDialog = false }) {
                            Text("Anuluj")
                        }
                        TextButton(onClick = {
                            showLocationDialog = false
                        }) {
                            Text("Zapisz")
                        }
                    }
                }
            },
            title = { Text("Dodaj lokalizację") },
            text = {
                Column {
                    TextField(value = zip, onValueChange = { zip = it }, label = { Text("Kod pocztowy") })
                    TextField(value = city, onValueChange = { city = it }, label = { Text("Miasto") })
                    TextField(value = street, onValueChange = { street = it }, label = { Text("Ulica") })
                    TextField(value = building, onValueChange = { building = it }, label = { Text("Numer budynku / mieszkania") })
                }
            }
        )
    }
}
