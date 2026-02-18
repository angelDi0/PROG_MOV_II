package com.example.myapplication.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.MainActivity
import com.example.marsphotos.data.SNRepository
import com.example.myapplication.SICENETApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import retrofit2.HttpException
import java.io.IOException
import kotlinx.serialization.json.Json

sealed interface SNUiState {
    data class Success(val accesoLogin: String) : SNUiState
    object Error : SNUiState
    object Loading : SNUiState
    object Idle : SNUiState
}

// 1. Enum para representar las pantallas de la aplicación.
public enum class AppScreen {
    Login,
    Home,
    CargaAcademica,
    Kardex,
    CalificacionesUnidad,
    CalificacionesFinales
}

private val jsonFormat = Json { ignoreUnknownKeys = true }

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
class SNViewModel(private val snRepository: SNRepository) : ViewModel() {
    var snUiState: SNUiState by mutableStateOf(SNUiState.Idle)
        private set

    // 2. Estado para gestionar la pantalla actual, la UI observará este estado.
    var currentScreen by mutableStateOf(AppScreen.Login)
        private set

    var matriculaInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")

    var alumnoData by mutableStateOf<DatosAlumno?>(null)
        private set

    var cargaAcademica by mutableStateOf<List<CargaAcademicaItem>>(emptyList())
        private set
    var kardex by mutableStateOf<List<KardexItem>>(emptyList())
        private set
    var calificacionesUnidad by mutableStateOf<List<CalificacionesUnidadItem>>(emptyList())
        private set
    var calificacionesFinales by mutableStateOf<List<CalificacionFinalItem>>(emptyList())
        private set

    fun onMatriculaChange(newValue: String) {
        matriculaInput = newValue
    }

    fun onPasswordChange(newValue: String) {
        passwordInput = newValue
    }

    fun accesoSN() {
        if (matriculaInput.isBlank() || passwordInput.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            snUiState = SNUiState.Loading
            snUiState = try {
                val listResult = snRepository.acceso(matriculaInput, passwordInput)
                if (listResult.isNotEmpty() && !listResult.contains("ERROR", ignoreCase = true)) {
                    // Al autenticarse, se navega a la pantalla Home y se cargan los datos del alumno.
                    currentScreen = AppScreen.Home
                    cargarDatosAlumno()
                    SNUiState.Success(listResult)
                } else {
                    SNUiState.Error
                }
            } catch (e: IOException) {
                SNUiState.Error
            } catch (e: HttpException) {
                SNUiState.Error
            }
        }
    }

    // 3. Función para ser llamada desde el menú de la UI.
    fun onMenuOptionSelected(screen: AppScreen) {
        currentScreen = screen
        // Carga los datos correspondientes a la pantalla si aún no han sido cargados.
        when (screen) {
            AppScreen.CargaAcademica -> if (cargaAcademica.isEmpty()) cargarCargaAcademica()
            AppScreen.Kardex -> if (kardex.isEmpty()) cargarKardex()
            AppScreen.CalificacionesUnidad -> if (calificacionesUnidad.isEmpty()) cargarCalificacionesUnidad()
            AppScreen.CalificacionesFinales -> if (calificacionesFinales.isEmpty()) cargarCalificacionesFinales()
            else -> { /* No action needed for Login or Home */ }
        }
    }

    // 4. Función para resetear el estado y volver al Login (para un botón de Logout).
    fun resetToLogin() {
        snUiState = SNUiState.Idle
        currentScreen = AppScreen.Login
        matriculaInput = ""
        passwordInput = ""
        alumnoData = null
        cargaAcademica = emptyList()
        kardex = emptyList()
        calificacionesUnidad = emptyList()
        calificacionesFinales = emptyList()
    }

    fun cargarDatosAlumno(){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val result = snRepository.datos_alumno()
                alumnoData = jsonFormat.decodeFromString<DatosAlumno>(result)
            } catch (e: Exception){
                Log.e("SNViewModel", "Error al cargar datos del alumno", e)
            }
        }
    }

    fun cargarCargaAcademica() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = snRepository.getCargaAcademica()
                cargaAcademica = jsonFormat.decodeFromString(result)
            } catch (e: Exception) {
                Log.e("SNViewModel", "Error al cargar la carga académica", e)
            }
        }
    }

    fun cargarKardex() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = snRepository.getKardex()
                kardex = jsonFormat.decodeFromString(result)
            } catch (e: Exception) {
                Log.e("SNViewModel", "Error al cargar el kardex", e)
            }
        }
    }

    fun cargarCalificacionesUnidad() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = snRepository.getCalificacionesUnidad()
                calificacionesUnidad = jsonFormat.decodeFromString(result)
            } catch (e: Exception) {
                Log.e("SNViewModel", "Error al cargar calificaciones por unidad", e)
            }
        }
    }

    fun cargarCalificacionesFinales() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = snRepository.getCalificacionesFinales()
                calificacionesFinales = jsonFormat.decodeFromString(result)
            } catch (e: Exception) {
                Log.e("SNViewModel", "Error al cargar calificaciones finales", e)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SICENETApplication)
                val snRepository = application.container.snRepository
                SNViewModel(snRepository = snRepository)
            }
        }
    }
}

@Serializable
data class DatosAlumno(
    val fechaReins: String = "",
    val modEducativo: Int = 0,
    val adeudo: Boolean = false,
    val urlFoto: String = "",
    val adeudoDescripcion: String = "",
    val inscrito: Boolean = false,
    val estatus: String = "",
    val semActual: Int = 0,
    val cdtosAcumulados: Int = 0,
    val cdtosActuales: Int = 0,
    val especialidad: String = "",
    val carrera: String = "",
    val lineamiento: Int = 0,
    val nombre: String = "",
    val matricula: String = ""
)

// Data classes for new features
@Serializable
data class CargaAcademicaItem(
    val Semipresencial: String,
    val Observaciones: String,
    val Docente: String,
    val clvOficial: String,
    val Sabado: String,
    val Viernes: String,
    val Jueves: String,
    val Miercoles: String,
    val Martes: String,
    val Lunes: String,
    val EstadoMateria: String,
    val CreditosMateria: Int,
    val Materia: String,
    val Grupo: String,
)

@Serializable
data class KardexItem(
    val S3: String? = null,
    val P3: String? = null,
    val A3: String? = null,
    val ClvMat: String,
    val ClvOfiMat: String,
    val Materia: String,
    val Cdts: Int,
    val Calif: Int,
    val Acred: String,
    val S1: String? = null,
    val P1: String? = null,
    val A1: String? = null,
    val S2: String? = null,
    val P2: String? = null,
    val A2: String? = null,
)

@Serializable
data class CalificacionesUnidadItem(
    val Observaciones: String,
    val C13: String? = null,
    val C12: String? = null,
    val C11: String? = null,
    val C10: String? = null,
    val C9: String? = null,
    val C8: String? = null,
    val C7: String? = null,
    val C6: String? = null,
    val C5: String? = null,
    val C4: String? = null,
    val C3: String? = null,
    val C2: String? = null,
    val C1: String? = null,
    val UnidadesActivas: String,
    val Materia: String,
    val Grupo: String,
)

@Serializable
data class CalificacionFinalItem(
    val calif: Int,
    val acred: String,
    val grupo: String,
    val materia: String,
    val Observaciones: String,
)
