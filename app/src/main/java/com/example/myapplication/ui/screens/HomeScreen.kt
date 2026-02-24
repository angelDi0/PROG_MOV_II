// Archivo: app/src/main/java/com/example/marsphotos/ui/screens/HomeScreen.kt
package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.DB.Entidad.Estudiante
import com.example.myapplication.viewmodel.SNViewModel
import kotlinx.serialization.InternalSerializationApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(InternalSerializationApi::class)
@Composable
fun HomeScreen(
    viewModel: SNViewModel,
    onNavigateToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.cargarDatosAlumno() }

    val alumno = viewModel.alumnoData

    if (alumno == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFFDFDFD))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "¡Hola,", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Text(
                        text = alumno.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // MUESTA LA FECHA DE LA ULTIMA ACTUALIAZCION

                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val fechaFormateada = sdf.format(Date(alumno.lastUpdated))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Actualizado: $fechaFormateada",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                AsyncImage(
                    model = "https://sicenet.surguanajuato.tecnm.mx/fotos/${alumno.urlFoto}",
                    contentDescription = null,
                    modifier = Modifier.size(55.dp).clip(CircleShape).background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AcademicProgressCard(alumno)

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Panel de Control", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuTile(
                        icon = Icons.Default.AccountBox,
                        title = "Semestre",
                        value = "${alumno.semActual}°",
                        color = Color(0xFFE3F2FD),
                        iconColor = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    )
                    MenuTile(
                        icon = Icons.Default.CheckCircle,
                        title = "Estatus",
                        value = alumno.estatus,
                        color = Color(0xFFF1F8E9),
                        iconColor = Color(0xFF388E3C),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuTile(
                        icon = Icons.Default.Info,
                        title = "Modalidad",
                        value = if(alumno.modEducativo == 2) "Presencial" else "Dual",
                        color = Color(0xFFFFF3E0),
                        iconColor = Color(0xFFF57C00),
                        modifier = Modifier.weight(1f)
                    )
                    MenuTile(
                        icon = Icons.Default.List,
                        title = "Créditos: ",
                        value = "${alumno.cdtosActuales}",
                        color = Color(0xFFF3E5F5),
                        iconColor = Color(0xFF7B1FA2),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.DarkGray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Próxima Reinscripción", fontSize = 12.sp, color = Color.Gray)
                        Text(text = alumno.fechaReins, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onNavigateToMenu() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ir al Menú")
            }
        }
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
fun AcademicProgressCard(alumno: Estudiante) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)) // Fondo Negro Elegante
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Progreso de Carrera", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(text = alumno.carrera, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(16.dp))

            val progress = alumno.cdtosAcumulados / 260f // Asumiendo 260 créditos totales
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color(0xFFBB86FC),
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "${alumno.cdtosAcumulados} créditos", color = Color.White, fontSize = 12.sp)
                Text(text = "Meta: 260", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MenuTile(icon: ImageVector, title: String, value: String, color: Color, iconColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 11.sp, color = Color.Black.copy(alpha = 0.6f))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
        }
    }
}
