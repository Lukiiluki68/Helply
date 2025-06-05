package com.example.helplyt.presentation.common

import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapSinglePinView(
    address: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationLatLng = remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(address) {
        Log.d("MAP_DEBUG", "Próba geokodowania dla: $address")
        val geocoder = Geocoder(context)
        try {
            val result = geocoder.getFromLocationName(address, 1)?.firstOrNull()
            if (result != null) {
                locationLatLng.value = LatLng(result.latitude, result.longitude)
                Log.d("MAP_DEBUG", "Znaleziono lokalizację: ${result.latitude}, ${result.longitude}")
            } else {
                Log.e("MAP_DEBUG", "Nie znaleziono współrzędnych.")
            }
        } catch (e: Exception) {
            Log.e("MAP_DEBUG", "Błąd geokodowania: ${e.message}")
        }
    }

    locationLatLng.value?.let { latLng ->
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(latLng, 14f)
        }

        GoogleMap(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = latLng),
                title = "Lokalizacja ogłoszenia"
            )
        }
    }
}

