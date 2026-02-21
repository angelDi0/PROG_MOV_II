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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.marsphotos.data.SNRepository
import com.example.myapplication.SICENETApplication
import com.example.myapplication.worker.SaveProfileDataWorker
import com.example.myapplication.worker.SyncProfileDataWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID


// 1. Se añade el estado Syncing y se ajusta Success para ser más genérico.
sealed interface SNUiState {
    data class Success(val message: String) : SNUiState
    object Error : SNUiState
    object Loading : SNUiState
    object Idle : SNUiState
    data class Syncing(val message: String) : SNUiState
}

// 2. Constantes para WorkManager.
private const val UNIQUE_PROFILE_SYNC_WORK = "UniqueProfileSyncWork"
private const val KEY_MATRICULA = "key_matricula"
private const val KEY_PASSWORD = "key_password"
private const val KEY_SYNC_TIMESTAMP = "key_sync_timestamp"


// Enum para representar las pantallas de la aplicación.
enum class AppScreen {
    Login,
    Home,
    CargaAcademica,
    Kardex,
    CalificacionesUnidad,
    CalificacionesFinales
}

private val jsonFormat = Json { ignoreUnknownKeys = true }

@OptIn(InternalSerializationApi::class)
class SNViewModel(
    private val snRepository: SNRepository,
    private val workManager: WorkManager // 3. Se inyecta una instancia de WorkManager.
) : ViewModel() {
    var snUiState: SNUiState by mutableStateOf(SNUiState.Idle)
        private set

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

    // 4. Nuevos estados para la fecha de la última sincronización y para observar el Worker.
    var lastSyncDate by mutableStateOf<String?>(null)
        private set
    var profileSyncWorkInfo by mutableStateOf<WorkInfo?>(null)
        private set

    fun onMatriculaChange(newValue: String) {
        matriculaInput = newValue
    }

    fun onPasswordChange(newValue: String) {
        passwordInput = newValue
    }

    // 5. Se modifica accesoSN para usar WorkManager.
    fun accesoSN() {
        if (matriculaInput.isBlank() || passwordInput.isBlank()) return

        // TODO: Añadir lógica para comprobar la conexión a internet.

        snUiState = SNUiState.Syncing("Autenticando y sincronizando perfil...")

        val inputData = workDataOf(
            KEY_MATRICULA to matriculaInput,
            KEY_PASSWORD to passwordInput
        )

        // Worker para obtener datos de la API. Su salida será la entrada del siguiente.
        val syncProfileWorker = OneTimeWorkRequestBuilder<SyncProfileDataWorker>()
            .setInputData(inputData)
            .build()

        // Worker para guardar los datos en la base de datos local.
        val saveProfileWorker = OneTimeWorkRequestBuilder<SaveProfileDataWorker>().build()

        workManager
            .beginUniqueWork(
                UNIQUE_PROFILE_SYNC_WORK,
                ExistingWorkPolicy.REPLACE,
                syncProfileWorker
            )
            .then(saveProfileWorker)
            .enqueue()

        // Se observa el estado del primer worker para actualizar la UI.
        observeProfileSync(syncProfileWorker.id)
    }

    // 6. Nueva función para observar el estado del Worker.
    private fun observeProfileSync(workId: UUID) {
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workId).collect { workInfo ->
                profileSyncWorkInfo = workInfo
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            snUiState = SNUiState.Success("Sincronización completada.")
                            // Ahora que la sincronización terminó, se cargan los datos desde la BD local.
                            cargarDatosAlumno()
                            lastSyncDate = workInfo.outputData.getString(KEY_SYNC_TIMESTAMP)
                            currentScreen = AppScreen.Home // Navegar a Home al terminar
                        }
                        WorkInfo.State.FAILED -> snUiState = SNUiState.Error
                        else -> { /* El estado ya es Syncing */ }
                    }
                }
            }
        }
    }

    fun onMenuOptionSelected(screen: AppScreen) {
        currentScreen = screen
        // Carga los datos correspondientes a la pantalla si aún no han sido cargados.
        // TODO: Implementar la lógica de WorkManager también para estas opciones como pide el punto 2b.
        when (screen) {
            AppScreen.CargaAcademica -> if (cargaAcademica.isEmpty()) cargarCargaAcademica()
            AppScreen.Kardex -> if (kardex.isEmpty()) cargarKardex()
            AppScreen.CalificacionesUnidad -> if (calificacionesUnidad.isEmpty()) cargarCalificacionesUnidad()
            AppScreen.CalificacionesFinales -> if (calificacionesFinales.isEmpty()) cargarCalificacionesFinales()
            else -> { /* No action needed for Login or Home */ }
        }
    }

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
                // Esta función ahora debe obtener los datos desde el repositorio local (Base de Datos).
                val result = snRepository.datos_alumno()
                alumnoData = jsonFormat.decodeFromString<DatosAlumno>(result)
            } catch (e: Exception){
                Log.e("SNViewModel", "Error al cargar datos del alumno desde la BD", e)
            }
        }
    }

    fun cargarCargaAcademica() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Esta y las demás funciones de carga ahora consultan la BD local.
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
                Log.d("KARDEX_JSON", result)

                if (result.isNullOrBlank()) {
                    Log.d("KARDEX", "El servicio regresó vacío")
                    kardex = emptyList()
                    return@launch
                }

                kardex = jsonFormat.decodeFromString(result)

                Log.d("KARDEX_SIZE", kardex.size.toString())

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
                // 7. Se provee la instancia de WorkManager al ViewModel.
                val workManager = WorkManager.getInstance(application)
                SNViewModel(snRepository = snRepository, workManager = workManager)
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
