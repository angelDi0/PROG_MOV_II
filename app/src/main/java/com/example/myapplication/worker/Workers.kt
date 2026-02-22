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
private const val TAG = "SyncWorkers"

private val jsonParser = Json { ignoreUnknownKeys = true }

/**
 * Worker para el perfil (Login)
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

/**
 * Worker para guardar el perfil
 */
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
 * Worker TODO-EN-UNO: Descarga y guarda solo si hay cambios.
 */
class SyncDatosWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val tipo = inputData.getString("tipo_dato") ?: return Result.failure()
        val lineamiento = inputData.getInt("lineamiento", 0)

        val app = (applicationContext as SICENETApplication)
        val repo = app.container.snRepository
        val db = app.container.database

        return try {
            // 1. Descarga del servidor
            val res = when(tipo) {
                "KARDEX" -> repo.getKardex(lineamiento)
                "CALIF_UNIDAD" -> repo.getCalificacionesUnidad()
                "CALIF_FINAL" -> repo.getCalificacionesFinales()
                "CARGA_ACADEMICA" -> repo.getCargaAcademica()
                else -> ""
            }

            if (res.isBlank()) return Result.failure()

            // 2. Lógica de comparación y guardado
            when(tipo) {
                "KARDEX" -> {
                    val response = if (res.trim().startsWith("[")) {
                        KardexResponse(jsonParser.decodeFromString<List<KardexItem>>(res))
                    } else {
                        jsonParser.decodeFromString<KardexResponse>(res)
                    }

                    // OBTENER DATOS ACTUALES DE LA DB
                    val datosLocales = db.kardexDao().getKardexSync()

                    // SOLO GUARDAR SI HAY CAMBIOS
                    if (response.lstKardex != datosLocales) {
                        Log.d(TAG, "Cambios detectados en KARDEX. Actualizando...")
                        db.kardexDao().eliminarTodo() // Opcional: limpiar antes de insertar
                        response.lstKardex.forEach { db.kardexDao().insertarKardex(it) }
                    } else {
                        Log.d(TAG, "KARDEX sin cambios. Omitiendo guardado.")
                    }
                }

                "CALIF_UNIDAD" -> {
                    val itemsNuevos = jsonParser.decodeFromString<List<CalificacionesUnidadItem>>(res)
                    val itemsLocales = db.calificacionesUnidadDao().getCalificacionesUnidadSync()

                    if (itemsNuevos != itemsLocales) {
                        Log.d(TAG, "Cambios en CALIF_UNIDAD. Actualizando...")
                        db.calificacionesUnidadDao().eliminarTodo()
                        itemsNuevos.forEach { db.calificacionesUnidadDao().insertarCalificacionesUnidad(it) }
                    }
                }

                "CALIF_FINAL" -> {
                    val itemsNuevos = jsonParser.decodeFromString<List<CalificacionFinalItem>>(res)
                    val itemsLocales = db.calificacionesFinalDao().getCalificacionesFinalesSync()

                    if (itemsNuevos != itemsLocales) {
                        db.calificacionesFinalDao().eliminarTodo()
                        itemsNuevos.forEach { db.calificacionesFinalDao().insertarCalificacionFinal(it) }
                    }
                }

                "CARGA_ACADEMICA" -> {
                    val itemsNuevos = jsonParser.decodeFromString<List<CargaAcademica>>(res)
                    val itemsLocales = db.cargaAcademicaDao().getCargaAcademicaSync()

                    if (itemsNuevos != itemsLocales) {
                        db.cargaAcademicaDao().eliminarTodo()
                        itemsNuevos.forEach { db.cargaAcademicaDao().insertarCargaAcademica(it) }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando $tipo", e)
            Result.failure()
        }
    }
}