package com.example.myapplication.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myapplication.SICENETApplication
import com.example.myapplication.network.bodyacceso
import com.example.myapplication.viewmodel.DatosAlumno
import kotlin.text.format
import kotlinx.serialization.json.Json

interface SNInterface {

}

private val json = Json { ignoreUnknownKeys = true }

class FetchAutorizacionWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val mat = inputData.getString("matricula") ?: return Result.failure()
        val pass = inputData.getString("password") ?: return Result.failure()

        val apiService = (applicationContext as SICENETApplication).container.snRepository

        return try {
            // Suponiendo que usas la lógica de extracción XML que discutimos antes
            val soapBody = bodyacceso.format(mat, pass)
            val rawResponse = apiService.acceso(mat, pass)
            val resultXml = rawResponse.substringAfter("<accesoLoginResult>").substringBefore("</accesoLoginResult>")

            // Pasamos el resultado al siguiente worker
            val output = workDataOf("json_data" to resultXml, "mat" to mat)
            Result.success(output)
        } catch (e: Exception) {
            Result.retry()
        }
    }
}


class GuardarDatosWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // 1. Recibe el JSON como dato de ENTRADA
        val perfilJson = inputData.getString("perfil_json") ?: return Result.failure()

        // Accede al DAO a través del Application
        val alumnoDao = (applicationContext as SICENETApplication).container.database.perfilDao()

        return try {
            // 2. Convierte el JSON a objeto y lo guarda
            val alumno = json.decodeFromString<DatosAlumno>(perfilJson)
            alumnoDao.insertarDatosPerfil(alumno.copy(lastUpdated = System.currentTimeMillis()))
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}