package com.example.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myapplication.DB.Entidad.Estudiante
import com.example.myapplication.SICENETApplication
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constantes para la comunicación entre Workers
private const val KEY_MATRICULA = "key_matricula"
private const val KEY_PASSWORD = "key_password"
private const val KEY_PROFILE_DATA = "key_profile_data"
private const val KEY_SYNC_TIMESTAMP = "key_sync_timestamp"
private const val TAG = "SyncWorkers"

private val jsonParser = Json { ignoreUnknownKeys = true }

/**
 * Primer Worker: Obtiene los datos del perfil y del alumno desde la API de SICENET.
 */
class SyncProfileDataWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        // Obtiene la matrícula y contraseña de los datos de entrada
        val matricula = inputData.getString(KEY_MATRICULA)
        val password = inputData.getString(KEY_PASSWORD)

        if (matricula.isNullOrBlank() || password.isNullOrBlank()) {
            Log.e(TAG, "Matrícula o contraseña no proporcionadas al Worker")
            return Result.failure()
        }

        // Obtiene una instancia del repositorio desde la clase Application
        val repository = (applicationContext as SICENETApplication).container.snRepository

        return try {
            // 1. Autenticación
            val loginResult = repository.acceso(matricula, password)
            if (loginResult.contains("ERROR", ignoreCase = true)){
                Log.e(TAG, "Error de autenticación en SyncProfileDataWorker")
                return Result.failure()
            }

            // 2. Obtención de datos del alumno
            val profileData = repository.datos_alumno()

            Log.d(TAG, "Datos del perfild de estudiante antes de ser mandadoL: $profileData")

            // 3. Formatear la fecha actual para la UI
            val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

            // 4. Pasar los datos del perfil y la fecha al siguiente worker
            val outputData = workDataOf(
                KEY_PROFILE_DATA to profileData,
                KEY_SYNC_TIMESTAMP to timestamp
            )

            Result.success(outputData)
        } catch (e: Exception) {
            Log.e(TAG, "Error en SyncProfileDataWorker", e)
            Result.failure()
        }
    }
}

/**
 * Segundo Worker: Guarda los datos del perfil en la base de datos local.
 */
class SaveProfileDataWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val profileData = inputData.getString(KEY_PROFILE_DATA)
        val timestamp = inputData.getString(KEY_SYNC_TIMESTAMP)

        if (profileData.isNullOrBlank() || timestamp.isNullOrBlank()) {
            Log.e(TAG, "No se recibieron datos del perfil para guardar")
            return Result.failure()
        }

        return try {

            val estudiante = jsonParser.decodeFromString<Estudiante>(profileData)
            val dao = (applicationContext as SICENETApplication).container.database.perfilDao()

            Log.d("TAG", "Objetos parseados: $estudiante")

            dao.insertarDatosPerfil(estudiante)

            Log.d(TAG, "Datos del perfil guardados en la base de datos local.")


            val outputData = workDataOf(KEY_SYNC_TIMESTAMP to timestamp)
            Result.success(outputData)
        } catch (e: Exception) {
            Log.e(TAG, "Error en SaveProfileDataWorker", e)
            Result.failure()
        }
    }
}
