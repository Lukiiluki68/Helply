package com.example.helplyt.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeAddressScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val currentAddress by viewModel.address.collectAsState()

    var postalCode by remember { mutableStateOf(currentAddress.postalCode) }
    var city by remember { mutableStateOf(currentAddress.city) }
    var street by remember { mutableStateOf(currentAddress.street) }
    var buildingNumber by remember { mutableStateOf(currentAddress.number) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adres użytkownika") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć"
                        )
                    }
                }
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
                value = postalCode,
                onValueChange = { postalCode = it },
                label = { Text("Kod pocztowy") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Miasto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text("Ulica") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = buildingNumber,
                onValueChange = { buildingNumber = it },
                label = { Text("Nr budynku / mieszkania") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Aktualizacja lokalna ViewModelu
                    viewModel.updateAddress(
                        postalCode = postalCode,
                        city = city,
                        street = street,
                        number = buildingNumber
                    )

                    // TODO: Zapisz dane adresowe do Firebase (np. Firestore)
                    // Można to zrobić wewnątrz viewModel.updateAddress lub osobną metodą

                    navController.popBackStack() // Wracamy do profilu
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Zapisz adres")
            }
        }
    }
}
