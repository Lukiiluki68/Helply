import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdScreen(
    navController: NavController,
    viewModel: CreateAdViewModel = viewModel() ,
            onBack: () -> Unit = { navController.popBackStack() }
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            date = "%02d-%02d-%04d".format(selectedDay, selectedMonth + 1, selectedYear)
        },
        year,
        month,
        day
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj ogłoszenie") },
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            TextField(value = title, onValueChange = { title = it }, label = { Text("Tytuł") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = description, onValueChange = { description = it }, label = { Text("Opis") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = price, onValueChange = { price = it }, label = { Text("Cena") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text("Data wykonania") },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Kalendarz")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { launcher.launch("image/*") }) {
                Text("Wybierz zdjęcie")
            }

            imageUri?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Wybrano zdjęcie: ${it.lastPathSegment}")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                viewModel.createAd(title, description, price, date, imageUri, context)
                navController.popBackStack()
            }) {
                Text("Dodaj ogłoszenie")
            }
        }
    }
}