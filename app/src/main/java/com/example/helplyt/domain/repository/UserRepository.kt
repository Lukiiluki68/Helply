package com.example.app.domain.repository

import android.net.Uri
import com.example.helplyt.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun getUserProfile(): UserProfile?
    suspend fun uploadAvatar(uri: Uri): Uri?
    suspend fun changePassword(currentPassword: String, newPassword: String)

}
