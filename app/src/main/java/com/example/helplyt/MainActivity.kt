package com.example.helplyt

import ChangePasswordUseCase
import GetUserProfileUseCase
import LoginViewModel
import SaveUserProfileUseCase
import UploadAvatarUseCase
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.app.data.repository.AuthRepository
import com.example.app.data.repository.UserRepositoryImpl
import com.example.app.domain.repository.AuthRepositoryImpl
import com.example.app.domain.use_case.LoginUseCase
import com.example.app.domain.use_case.RegisterUseCase
import com.example.app.navigation.AppNavigation
import com.example.app.presentation.register.RegisterViewModel
import com.example.helplyt.presentation.profile.ProfileViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            setContent {
                com.example.helplyt.ui.theme.HelplyTTheme(
                    darkTheme = isSystemInDarkTheme(), // lub false, jeśli chcesz wymusić jasny
                    dynamicColor = false // bardzo ważne!
                ) {
                    val auth = FirebaseAuth.getInstance()
                    val authRepository = AuthRepositoryImpl(auth)
                    val userRepository = UserRepositoryImpl()  // <-- najpierw zainicjalizuj userRepository

                    val loginViewModel = remember {
                        LoginViewModel(
                            loginUseCase = LoginUseCase(authRepository),
                            userRepository = userRepository       // <-- potem użyj go tutaj
                        )
                    }

                    val registerViewModel = remember { RegisterViewModel(RegisterUseCase(authRepository)) }

                    val profileViewModel = remember {
                        ProfileViewModel(
                            getUserProfile = GetUserProfileUseCase(userRepository),
                            saveUserProfileUseCase = SaveUserProfileUseCase(userRepository),
                            uploadAvatar = UploadAvatarUseCase(userRepository),
                            changePassword = ChangePasswordUseCase(userRepository)
                        )
                    }

                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        loginViewModel = loginViewModel,
                        registerViewModel = registerViewModel,
                        profileViewModel = profileViewModel,
                    )
                }
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Init error", e)
            setContent {
                Text("Initialization error: ${e.localizedMessage}")
            }
        }
    }
}