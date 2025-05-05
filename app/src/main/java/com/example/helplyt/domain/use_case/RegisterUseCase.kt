package com.example.app.domain.use_case
import com.example.app.data.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = repository.register(email, password)
}
