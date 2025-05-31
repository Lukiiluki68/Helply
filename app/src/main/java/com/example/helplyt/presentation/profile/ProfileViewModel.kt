package com.example.helplyt.presentation.profile

import ChangePasswordUseCase
import GetUserProfileUseCase
import SaveUserProfileUseCase
import UploadAvatarUseCase
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helplyt.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserProfile: GetUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val uploadAvatar: UploadAvatarUseCase,
    private val changePassword: ChangePasswordUseCase,
) : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    init {
        viewModelScope.launch {
            val profile = getUserProfile()
            if (profile != null) {
                _profile.value = profile
            }
        }
    }

    fun updateAddress(postalCode: String, city: String, street: String, number: String) {
        _profile.value = _profile.value.copy(
            postalCode = postalCode,
            city = city,
            street = street,
            number = number
        )
        viewModelScope.launch {
            saveUserProfile(_profile.value)
        }
    }

    fun setAvatar(uri: Uri) {
        viewModelScope.launch {
            try {
                val url = uploadAvatar(uri)
                if (url != null) {
                    _profile.value = _profile.value.copy(avatarUrl = url.toString())
                    saveUserProfile(_profile.value)
                } else {
                    // loguj lub pokaż błąd użytkownikowi
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun changePassword(current: String, new: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                changePassword(current, new)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }


    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            saveUserProfileUseCase(profile)
        }
    }
    fun loadUserProfile() {
        viewModelScope.launch {
            val profile = getUserProfile()
            if (profile != null) {
                _profile.value = profile
            }
        }
    }
}
