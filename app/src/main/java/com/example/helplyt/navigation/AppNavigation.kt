package com.example.app.navigation

import AdvertisementScreen
import CreateAdScreen
import LoginScreen
import LoginViewModel
import ProfileScreen
import RegisterScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app.presentation.home.HomeScreen
import com.example.app.presentation.register.RegisterViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreateAd : Screen("createAd")
    object Advertisement : Screen("advertisement")
    object Profile : Screen("profile")

}

@Composable
fun AppNavigation(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { navController.navigate(Screen.Home.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = { navController.navigate(Screen.Home.route) },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController
            )
        }
        composable(Screen.CreateAd.route) {
            CreateAdScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Advertisement.route) {
            AdvertisementScreen(
                navController = navController,
             //   onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
            )
        }

    }
}
