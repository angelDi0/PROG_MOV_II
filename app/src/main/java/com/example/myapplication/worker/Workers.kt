package com.example.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myapplication.SICENETApplication
import com.example.myapplication.network.SICENETWService
import com.example.myapplication.network.bodyacceso
import kotlin.text.format

interface SNInterface {

}

class FetchAutorizacionWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val mat = inputData.getString("mat") ?: return Result.failure()
        val pass = inputData.getString("pass") ?: return Result.failure()

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

class GuardarDatosWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val jsonData = inputData.getString("json_data") ?: return Result.failure()
        val mat = inputData.getString("mat") ?: ""

        return try {
            val dao = (applicationContext as SICENETApplication).container.snRepository

            Log.d("Worker", "Datos Guardados Localmente")
            Result.success()
        } catch (e: Exception){
            Log.e("Worker", "Error al guardar en DB local: ${e.message}")
            Result.failure()
        }
    }
}