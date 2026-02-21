// Archivo: app/src/main/java/com/example/marsphotos/ui/screens/LoginScreen.kt
package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SNUiState
import com.example.myapplication.viewmodel.SNViewModel

@Composable
fun LoginScreen(
    viewModel: SNViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Inicio de Sesión SICENET", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.matriculaInput,
            onValueChange = { viewModel.onMatriculaChange(it) },
            label = { Text("Matrícula") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.passwordInput,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.accesoSN(context) },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.snUiState !is SNUiState.Loading
        ) {
            if (viewModel.snUiState is SNUiState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Entrar")
            }
        }

        if (viewModel.snUiState is SNUiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Error: Verifica tus credenciales", color = MaterialTheme.colorScheme.error)
        }
    }
}