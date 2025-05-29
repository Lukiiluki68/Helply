package com.example.helplyt.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val _username = MutableStateFlow("Jan Kowalski")
    val username: StateFlow<String> = _username

    private val _email = MutableStateFlow("jan.kowalski@example.com")
    val email: StateFlow<String> = _email

    private val _avatarUri = MutableStateFlow<Uri?>(null)
    val avatarUri: StateFlow<Uri?> = _avatarUri

    fun setAvatar(uri: Uri) {
        _avatarUri.value = uri
    }

    // ADRES — tymczasowe dane lokalne
    data class Address(
        val postalCode: String = "",
        val city: String = "",
        val street: String = "",
        val number: String = ""
    )

    private val _address = MutableStateFlow(Address())
    val address: StateFlow<Address> = _address

    fun updateAddress(postalCode: String, city: String, street: String, number: String) {
        _address.value = Address(postalCode, city, street, number)

        // TODO: Zapisz adres użytkownika do Firebase
    }

    // TODO: Załaduj dane adresowe z Firebase przy starcie i ustaw _address.value
}
