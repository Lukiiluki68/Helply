package com.example.app.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.use_case.RegisterUseCase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    var registerResult: Result<FirebaseUser?>? = null
        private set

    fun register(email: String, password: String, onResult: (Result<FirebaseUser?>) -> Unit) {
        viewModelScope.launch {
            val result = registerUseCase(email, password)
            registerResult = result
            onResult(result)
        }
    }
}
