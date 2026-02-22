package com.example.myapplication.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.myapplication.ui.screens.*
import com.example.myapplication.viewmodel.SNUiState
import com.example.myapplication.viewmodel.SNViewModel

@Composable
fun SNApp() {

    val navController = rememberNavController()

    val snViewModel: SNViewModel =
        viewModel(factory = SNViewModel.Factory)

    Scaffold { padding ->

        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            composable("login") {
                LoginScreen(
                    viewModel = snViewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    viewModel = snViewModel,
                    onNavigateToMenu = {
                        navController.navigate("menu")
                    }
                )
            }

            composable("menu") {
                MenuScreen(
                    viewModel = snViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}