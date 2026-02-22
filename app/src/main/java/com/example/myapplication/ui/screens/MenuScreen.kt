package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.viewmodel.*
import androidx.compose.material3.*

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

        Text(
            text = "Panel Académico",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔷 CARDS DEL MENÚ
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            MenuCard(
                icon = Icons.Default.List,
                title = "Carga Académica",
                color = Color(0xFFE3F2FD),
                iconColor = Color(0xFF1976D2)
            ) {
                viewModel.onMenuOptionSelected(AppScreen.CargaAcademica)
            }

            MenuCard(
                icon = Icons.Default.AccountBox,
                title = "Kardex",
                color = Color(0xFFF3E5F5),
                iconColor = Color(0xFF7B1FA2)
            ) {
                viewModel.onMenuOptionSelected(AppScreen.Kardex)
            }

            MenuCard(
                icon = Icons.Default.CheckCircle,
                title = "Calificaciones Unidad",
                color = Color(0xFFFFF3E0),
                iconColor = Color(0xFFF57C00)
            ) {
                viewModel.onMenuOptionSelected(AppScreen.CalificacionesUnidad)
            }

            MenuCard(
                icon = Icons.Default.CheckCircle,
                title = "Calificaciones Finales",
                color = Color(0xFFE8F5E9),
                iconColor = Color(0xFF388E3C)
            ) {
                viewModel.onMenuOptionSelected(AppScreen.CalificacionesFinales)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🔷 CONTENIDO DINÁMICO
        when (currentScreen) {
            AppScreen.CargaAcademica -> CargaAcademicaTable(viewModel.cargaAcademica)
            AppScreen.Kardex -> KardexTable(viewModel.kardex)
            AppScreen.CalificacionesUnidad -> CalificacionesUnidadTable(viewModel.calificacionesUnidad)
            AppScreen.CalificacionesFinales -> CalificacionesFinalesTable(viewModel.calificacionesFinales)
            else -> {}
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver")
        }
    }
}

@Composable
fun MenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/* ========================= */
/* 🔷 TABLAS ESTILIZADAS */
/* ========================= */

@Composable
fun CargaAcademicaTable(lista: List<CargaAcademicaItem>) {

    SectionTitle("Carga Académica")

    if (lista.isEmpty()) {
        EmptyState()
        return
    }

    lista.forEach { item ->
        InfoCard {
            Text(item.Materia, fontWeight = FontWeight.Bold)
            Text("Grupo: ${item.Grupo}")
            Text("Docente: ${item.Docente}")
            Text("Créditos: ${item.CreditosMateria}")
            Text("Estado: ${item.EstadoMateria}")
        }
    }
}

@Composable
fun KardexTable(lista: List<KardexItem>) {

    SectionTitle("Kardex")

    if (lista.isEmpty()) {
        EmptyState()
        return
    }

    lista.forEach { item ->
        InfoCard {
            Text(item.Materia, fontWeight = FontWeight.Bold)
            Text("Créditos: ${item.Cdts}")
            Text("Calificación: ${item.Calif}")
            Text("Acreditada: ${item.Acred}")
        }
    }
}

@Composable
fun CalificacionesUnidadTable(lista: List<CalificacionesUnidadItem>) {

    SectionTitle("Calificaciones por Unidad")

    if (lista.isEmpty()) {
        EmptyState()
        return
    }

    lista.forEach { item ->
        InfoCard {
            Text(item.Materia, fontWeight = FontWeight.Bold)
            Text("Grupo: ${item.Grupo}")
        }
    }
}

@Composable
fun CalificacionesFinalesTable(lista: List<CalificacionFinalItem>) {

    SectionTitle("Calificaciones Finales")

    if (lista.isEmpty()) {
        EmptyState()
        return
    }

    lista.forEach { item ->
        InfoCard {
            Text(item.materia, fontWeight = FontWeight.Bold)
            Text("Grupo: ${item.grupo}")
            Text("Calificación: ${item.calif}")
            Text("Acreditada: ${item.acred}")
        }
    }
}

/* ========================= */
/* 🔷 COMPONENTES REUTILIZABLES */
/* ========================= */

@Composable
fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(text = text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No hay datos disponibles", color = Color.Gray)
    }
}