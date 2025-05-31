package com.example.helplyt.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.helplyt.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.text.SimpleDateFormat
import android.app.DatePickerDialog
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }

    var birthDate by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uzupełnij profil") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    firstNameError = it.length < 2
                },
                isError = firstNameError,
                label = { Text("Imię") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (firstNameError) Text("Imię musi mieć min. 2 znaki")
                }
            )


            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    lastNameError = it.length < 2
                },
                isError = lastNameError,
                label = { Text("Nazwisko") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (lastNameError) Text("Nazwisko musi mieć min. 2 znaki")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            calendar.set(selectedYear, selectedMonth, selectedDay)
                            birthDate = sdf.format(calendar.time)

                            birthDateError = !isAgeValid(calendar.time)
                        },
                        year, month, day
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (birthDate.isEmpty()) "Wybierz datę urodzenia" else birthDate)
            }

            if (birthDateError) {
                Text(
                    text = "Musisz mieć co najmniej 15 lat",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    firstNameError = firstName.length < 2
                    lastNameError = lastName.length < 2
                    birthDateError = birthDate.isEmpty() || !isAgeValid(calendar.time)

                    if (!firstNameError && !lastNameError && !birthDateError) {
                        val fullName = "$firstName $lastName"
                        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

                        viewModel.saveUserProfile(
                            UserProfile(
                                username = fullName,
                                email = email,
                                birthDate = birthDate
                            )
                        )
                        viewModel.loadUserProfile()

                        navController.navigate("home") {
                            popUpTo("setupProfile") { inclusive = true }
                        }
                    }

                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Zapisz i przejdź dalej")
            }
        }
    }
}
fun isAgeValid(date: java.util.Date): Boolean {
    val today = Calendar.getInstance()
    val birth = Calendar.getInstance()
    birth.time = date

    val age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
        return age - 1 >= 15
    }
    return age >= 15
}
