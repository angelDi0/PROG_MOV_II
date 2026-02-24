package com.example.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myapplication.DB.Entidad.CalificacionFinalItem
import com.example.myapplication.DB.Entidad.CalificacionesUnidadItem
import com.example.myapplication.DB.Entidad.CargaAcademica
import com.example.myapplication.DB.Entidad.Estudiante
import com.example.myapplication.DB.Entidad.KardexItem
import com.example.myapplication.SICENETApplication
import com.example.myapplication.viewmodel.KardexResponse
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val KEY_MATRICULA = "key_matricula"
private const val KEY_PASSWORD = "key_password"
private const val KEY_PROFILE_DATA = "key_profile_data"
private const val KEY_SYNC_TIMESTAMP = "key_sync_timestamp"
private const val KEY_TEMP_FILE = "key_temp_file" // Nueva constante para el nombre del archivo
private const val TAG = "SyncWorkers"

private val jsonParser = Json { ignoreUnknownKeys = true }

/**
 * Worker para el perfil (Login) - Los datos de perfil suelen ser pequeños, se pueden pasar por Data.
 */
class SyncProfileDataWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val matricula = inputData.getString(KEY_MATRICULA)
        val password = inputData.getString(KEY_PASSWORD)
        if (matricula.isNullOrBlank() || password.isNullOrBlank()) return Result.failure()
        
        val repository = (applicationContext as SICENETApplication).container.snRepository
        return try {
            val loginResult = repository.acceso(matricula, password)
            if (loginResult.contains("ERROR", ignoreCase = true)) return Result.failure()
            
            val profileData = repository.datos_alumno()
            val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            
            Result.success(workDataOf(KEY_PROFILE_DATA to profileData, KEY_SYNC_TIMESTAMP to timestamp))
        } catch (e: Exception) { 
            Log.e(TAG, "Error en SyncProfileDataWorker", e)
            Result.failure() 
        }
    }
}

class GuardarDatosPerfilWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val profileData = inputData.getString(KEY_PROFILE_DATA) ?: return Result.failure()
        return try {
            val estudiante = jsonParser.decodeFromString<Estudiante>(profileData)
            val dao = (applicationContext as SICENETApplication).container.database.perfilDao()
            dao.insertarDatosPerfil(estudiante)
            Result.success()
        } catch (e: Exception) { 
            Log.e(TAG, "Error en GuardarDatosPerfilWorker", e)
            Result.failure() 
        }
    }
}

/**
 * Worker 1: Descarga los datos y los guarda en un archivo interno para evitar el límite de 10KB.
 */
class FetchDataWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val tipo = inputData.getString("tipo_dato") ?: return Result.failure()
        val lineamiento = inputData.getInt("lineamiento", 0)
        val repo = (applicationContext as SICENETApplication).container.snRepository

        return try {
            val res = when(tipo) {
                "KARDEX" -> repo.getKardex(lineamiento)
                "CALIF_UNIDAD" -> repo.getCalificacionesUnidad()
                "CALIF_FINAL" -> repo.getCalificacionesFinales()
                "CARGA_ACADEMICA" -> repo.getCargaAcademica()
                else -> ""
            }

            if (res.isBlank()) return Result.failure()
            
            // ESCRIBIR EN ARCHIVO INTERNO
            val fileName = "temp_sync_${tipo}.json"
            applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use { 
                it.write(res.toByteArray()) 
            }

            // Solo pasamos el nombre del archivo, que es un String pequeño
            Result.success(workDataOf(KEY_TEMP_FILE to fileName, "tipo_dato" to tipo))
        } catch (e: Exception) {
            Log.e(TAG, "Error descargando $tipo", e)
            Result.failure()
        }
    }
}

/**
 * Worker 2: Lee el archivo temporal, compara y guarda en la DB.
 */
class SaveDataWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val fileName = inputData.getString(KEY_TEMP_FILE) ?: return Result.failure()
        val tipo = inputData.getString("tipo_dato") ?: return Result.failure()
        val db = (applicationContext as SICENETApplication).container.database

        return try {
            // LEER DESDE EL ARCHIVO
            val res = applicationContext.openFileInput(fileName).bufferedReader().use { it.readText() }

            when(tipo) {
                "KARDEX" -> {
                    val response = if (res.trim().startsWith("[")) {
                        KardexResponse(jsonParser.decodeFromString<List<KardexItem>>(res))
                    } else {
                        jsonParser.decodeFromString<KardexResponse>(res)
                    }
                    val datosLocales = db.kardexDao().getKardexSync()
                    if (response.lstKardex != datosLocales) {
                        db.kardexDao().eliminarTodo()
                        response.lstKardex.forEach { db.kardexDao().insertarKardex(it) }
                    }
                }
                "CALIF_UNIDAD" -> {
                    val nuevos = jsonParser.decodeFromString<List<CalificacionesUnidadItem>>(res)
                    val locales = db.calificacionesUnidadDao().getCalificacionesUnidadSync()
                    if (nuevos != locales) {
                        db.calificacionesUnidadDao().eliminarTodo()
                        nuevos.forEach { db.calificacionesUnidadDao().insertarCalificacionesUnidad(it) }
                    }
                }
                "CALIF_FINAL" -> {
                    val nuevos = jsonParser.decodeFromString<List<CalificacionFinalItem>>(res)
                    val locales = db.calificacionesFinalDao().getCalificacionesFinalesSync()
                    if (nuevos != locales) {
                        db.calificacionesFinalDao().eliminarTodo()
                        nuevos.forEach { db.calificacionesFinalDao().insertarCalificacionFinal(it) }
                    }
                }
                "CARGA_ACADEMICA" -> {
                    val nuevos = jsonParser.decodeFromString<List<CargaAcademica>>(res)
                    val locales = db.cargaAcademicaDao().getCargaAcademicaSync()
                    if (nuevos != locales) {
                        db.cargaAcademicaDao().eliminarTodo()
                        nuevos.forEach { db.cargaAcademicaDao().insertarCargaAcademica(it) }
                    }
                }
            }
            
            // BORRAR ARCHIVO TEMPORAL para liberar espacio
            applicationContext.deleteFile(fileName)
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando $tipo", e)
            Result.failure()
        }
    }
}
