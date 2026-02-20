package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.*

@Composable
fun MenuScreen(
    viewModel: SNViewModel,
    onBack: () -> Unit
) {

    val currentScreen = viewModel.currentScreen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text("Menú Académico", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // 🔵 BOTONES
        MenuButton("Carga Académica") {
            viewModel.onMenuOptionSelected(AppScreen.CargaAcademica)
        }

        MenuButton("Kardex") {
            viewModel.onMenuOptionSelected(AppScreen.Kardex)
        }

        MenuButton("Calificaciones Unidad") {
            viewModel.onMenuOptionSelected(AppScreen.CalificacionesUnidad)
        }

        MenuButton("Calificación Final") {
            viewModel.onMenuOptionSelected(AppScreen.CalificacionesFinales)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔵 CONTENIDO DINÁMICO
        when (currentScreen) {
            AppScreen.CargaAcademica -> CargaAcademicaTable(viewModel.cargaAcademica)
            AppScreen.Kardex -> KardexTable(viewModel.kardex)
            AppScreen.CalificacionesUnidad -> CalificacionesUnidadTable(viewModel.calificacionesUnidad)
            AppScreen.CalificacionesFinales -> CalificacionesFinalesTable(viewModel.calificacionesFinales)
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text)
    }
}

@Composable
fun CargaAcademicaTable(lista: List<CargaAcademicaItem>) {

    Text("Carga Académica", fontWeight = FontWeight.Bold)

    Spacer(modifier = Modifier.height(12.dp))

    lista.forEach { item ->

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                Text(item.Materia, fontWeight = FontWeight.Bold)
                Text("Grupo: ${item.Grupo}")
                Text("Docente: ${item.Docente}")
                Text("Créditos: ${item.CreditosMateria}")
                Text("Estado: ${item.EstadoMateria}")
            }
        }
    }
}

@Composable
fun KardexTable(lista: List<KardexItem>) {

    Text("Kardex", fontWeight = FontWeight.Bold)

    Spacer(modifier = Modifier.height(12.dp))

    lista.forEach { item ->

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                Text(item.Materia, fontWeight = FontWeight.Bold)
                Text("Créditos: ${item.Cdts}")
                Text("Calificación: ${item.Calif}")
                Text("Acreditada: ${item.Acred}")
            }
        }
    }
}

@Composable
fun CalificacionesUnidadTable(lista: List<CalificacionesUnidadItem>) {

    Text("Calificaciones por Unidad", fontWeight = FontWeight.Bold)

    Spacer(modifier = Modifier.height(12.dp))

    lista.forEach { item ->

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                Text(item.Materia, fontWeight = FontWeight.Bold)
                Text("Grupo: ${item.Grupo}")
                Text("U1: ${item.C1 ?: "-"}")
                Text("U2: ${item.C2 ?: "-"}")
                Text("U3: ${item.C3 ?: "-"}")
            }
        }
    }
}

@Composable
fun CalificacionesFinalesTable(lista: List<CalificacionFinalItem>) {

    Text("Calificaciones Finales", fontWeight = FontWeight.Bold)

    Spacer(modifier = Modifier.height(12.dp))

    lista.forEach { item ->

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                Text(item.materia, fontWeight = FontWeight.Bold)
                Text("Grupo: ${item.grupo}")
                Text("Calificación: ${item.calif}")
                Text("Acreditada: ${item.acred}")
            }
        }
    }
}
