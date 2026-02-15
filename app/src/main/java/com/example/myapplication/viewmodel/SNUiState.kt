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

private val jsonFormat = Json { ignoreUnknownKeys = true }

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
class SNViewModel(private val snRepository: SNRepository) : ViewModel() {
    var snUiState: SNUiState by mutableStateOf(SNUiState.Idle)
        private set

    var matriculaInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")

    var alumnoData by mutableStateOf<DatosAlumno?>(null)
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
