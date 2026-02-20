package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SNViewModel
import com.example.myapplication.viewmodel.SNUiState

@Composable
fun PantallaPrueba(viewModel: SNViewModel, modifier: Modifier = Modifier) {
    val alumno = viewModel.alumnoData
    val uiState = viewModel.snUiState
    val syncDate = viewModel.lastSyncDate

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Perfil del Alumno",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // INDICADOR DE SINCRONIZACIÓN (Para saber si el Worker está trabajando)
        if (uiState is SNUiState.Syncing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = uiState.message, color = MaterialTheme.colorScheme.primary)
            }
        }

        // FECHA DE ÚLTIMA ACTUALIZACIÓN (Requerimiento 2b)
        syncDate?.let {
            Text(
                text = "Última actualización: $it",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MOSTRAR DATOS (Si el Worker 2 guardó y cargarDatosAlumno leyó bien)
        if (alumno != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DatoItem("Nombre", alumno.nombre)
                    DatoItem("Matrícula", alumno.matricula)
                    DatoItem("Carrera", alumno.carrera)
                    DatoItem("Especialidad", alumno.especialidad)
                    DatoItem("Semestre", alumno.semActual.toString())
                    DatoItem("Créditos", "${alumno.cdtosAcumulados} acumulados")
                    DatoItem("Estatus", alumno.estatus)
                }
            }
        } else if (uiState is SNUiState.Idle || uiState is SNUiState.Success) {
            // Si no hay datos y no está cargando, algo falló en la BD o está vacío
            Text("No hay datos locales disponibles.", color = Color.Red)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.resetToLogin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun DatoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp)
    }
}