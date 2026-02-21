package com.example.myapplication.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.example.myapplication.DB.Entidad.Estudiante
import com.example.myapplication.SICENETApplication
import com.example.myapplication.worker.SaveProfileDataWorker
import com.example.myapplication.worker.SyncProfileDataWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

class SNViewModel(
    private val snRepository: SNRepository,
    private val workManager: WorkManager,
    private val application: SICENETApplication
) : ViewModel() {
    var snUiState: SNUiState by mutableStateOf(SNUiState.Idle)
        private set

    var currentScreen by mutableStateOf(AppScreen.Login)
        private set

    var matriculaInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")

    // Cambiamos alumnoData para que use la entidad Estudiante
    var alumnoData by mutableStateOf<Estudiante?>(null)

    var cargaAcademica by mutableStateOf<List<CargaAcademicaItem>>(emptyList())
        private set
    var kardex by mutableStateOf<List<KardexItem>>(emptyList())
        private set
    var calificacionesUnidad by mutableStateOf<List<CalificacionesUnidadItem>>(emptyList())
        private set
    var calificacionesFinales by mutableStateOf<List<CalificacionFinalItem>>(emptyList())
        private set

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

    fun accesoSN(context: Context) {
        if (matriculaInput.isBlank() || passwordInput.isBlank()) return

        viewModelScope.launch {
            if (!tieneInternet(context)) {
                Log.d("ISV", "No tiene internet!!")
                snUiState = SNUiState.Syncing("Sin conexión. Cargando datos locales...")
                cargarDatosAlumno()

                if (alumnoData != null) {
                    snUiState = SNUiState.Success("Datos locales cargados.")
                    currentScreen = AppScreen.Home
                } else {
                    snUiState = SNUiState.Error
                }
            } else {
                iniciarSincronizacionRemota(context)
            }
        }
    }

    private fun iniciarSincronizacionRemota(context: Context) {
        snUiState = SNUiState.Syncing("Autenticando y sincronizando...")

        val inputData = workDataOf(
            KEY_MATRICULA to matriculaInput,
            KEY_PASSWORD to passwordInput
        )

        val syncProfileWorker = OneTimeWorkRequestBuilder<SyncProfileDataWorker>()
            .setInputData(inputData)
            .build()

        val saveProfileWorker = OneTimeWorkRequestBuilder<SaveProfileDataWorker>().build()

        workManager
            .beginUniqueWork(UNIQUE_PROFILE_SYNC_WORK, ExistingWorkPolicy.REPLACE, syncProfileWorker)
            .then(saveProfileWorker)
            .enqueue()

        observeProfileSync(syncProfileWorker.id, context)
    }

    private fun observeProfileSync(workId: UUID, context: Context) {
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workId).collect { workInfo ->
                profileSyncWorkInfo = workInfo
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            guardarDatosSesion(context, matriculaInput, passwordInput)
                            snUiState = SNUiState.Success("Sincronización completada.")
                            cargarDatosAlumno()
                            if(alumnoData != null) {
                                currentScreen = AppScreen.Home
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            Log.w("SNViewModel", "Sincronización falló. Cargando locales.")
                            cargarDatosAlumno()
                            if (alumnoData != null) {
                                snUiState = SNUiState.Success("Mostrando datos locales.")
                                currentScreen = AppScreen.Home
                            } else {
                                snUiState = SNUiState.Error
                            }
                        }
                        else -> { /* En progreso... */ }
                    }
                }
            }
        }
    }

    fun onMenuOptionSelected(screen: AppScreen) {
        currentScreen = screen
        when (screen) {
            AppScreen.CargaAcademica -> if (cargaAcademica.isEmpty()) cargarCargaAcademica()
            AppScreen.Kardex -> if (kardex.isEmpty()) cargarKardex()
            AppScreen.CalificacionesUnidad -> if (calificacionesUnidad.isEmpty()) cargarCalificacionesUnidad()
            AppScreen.CalificacionesFinales -> if (calificacionesFinales.isEmpty()) cargarCalificacionesFinales()
            else -> { }
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

    suspend fun cargarDatosAlumno() {
        try {
            val dao = application.container.database.perfilDao()
            val estudianteLocal = withContext(Dispatchers.IO) {
                dao.getPerfilSync()
            }

            if (estudianteLocal != null) {
                alumnoData = estudianteLocal
            }
        } catch (e: Exception) {
            Log.e("SNViewModel", "Error al leer DB", e)
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

    fun cargarSesionGuardada(context: Context) {
        val shared = context.getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val m = shared.getString("matricula", "") ?: ""
        val p = shared.getString("password", "") ?: ""
        
        if (m.isNotBlank() && p.isNotBlank()) {
            matriculaInput = m
            passwordInput = p
            accesoSN(context)
        }
    }

    private fun tieneInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun guardarDatosSesion(context: Context, matricula: String, password: String){
        val shared = context.getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        with(shared.edit()){
            putString("matricula", matricula)
            putString("password", password)
            apply()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SICENETApplication)
                val snRepository = application.container.snRepository
                val workManager = WorkManager.getInstance(application)
                SNViewModel(
                    snRepository = snRepository, 
                    workManager = workManager,
                    application = application
                )
            }
        }
    }
}

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
