/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.data

import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.myapplication.DB.DAO.DaoEstudiante
import com.example.myapplication.network.SICENETWService
import com.example.myapplication.network.bodyacceso
import com.example.myapplication.worker.FetchAutorizacionWorker
import com.example.myapplication.worker.GuardarDatosWorker
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.comparisons.then

/**
 * Repository that fetch mars photos list from marsApi.
 */
interface SNRepository {
    /** Fetches list of MarsPhoto from marsApi */
    suspend fun acceso(m: String, p: String): String
    suspend fun datos_alumno(): String

}

/**
 * Clase para el uso del servicio de las funciones de SNNetwork
 */
class DBLocalSNRepository(val apiDB : Any):SNRepository {
    override suspend fun acceso(m: String, p: String): String {
        //TODO("Not yet implemented")
        //Reviso en base de datos
        //Preparar Room

        //apiDB.acceso( Usuario(matricula = m) )

        return ""

    }

    override suspend fun datos_alumno(): String {
        TODO("Not yet implemented")
    }
}

class SNReposiotory(
    private val apiService: SICENETWService,
    private val alumnoDao: DaoEstudiante,
    private val workManager: WorkManager
){
    fun perfilLocal(matricula: String) = alumnoDao.getPerfil(matricula)

    fun sincronizacionDeDatos(matricula: String, pass: String){
        val data = workDataOf("mat" to matricula, "pass" to pass)

        workManager.beginUniqueWork(
            "sync_auth_${matricula}",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<FetchAutorizacionWorker>().setInputData(data).build()
        ).then(
            OneTimeWorkRequestBuilder<GuardarDatosWorker>().build()
        ).enqueue()
    }
}

/**
 * Network Implementation of Repository that fetch mars photos list from marsApi.
 */
class NetworSNRepository(
    private val snApiService: SICENETWService,
) : SNRepository {
    /** Fetches list of MarsPhoto from marsApi*/
    override suspend fun acceso(m: String, p: String): String {
        return try {
            val res = snApiService.acceso(bodyacceso.format(m, p).toRequestBody())
//            Log.d("Datos", "Datos del usuario: ${m} ${p}")


            // Convertimos a string
            val xmlResponse = res.string()
            Log.d("RXML", "XML Completo: $xmlResponse")

            // Extraemos los datos
            if (xmlResponse.contains("<accesoLoginResult>")) {
                val resultado = xmlResponse
                    .substringAfter("<accesoLoginResult>")
                    .substringBefore("</accesoLoginResult>")

                Log.d("RXML", "Resultado extraído: $resultado")
                resultado
            } else {
                Log.e("RXML", "No se encontró la etiqueta accesoLoginResult")
                ""
            }
        } catch (e: Exception) {
            Log.e("RXML", "Error en la petición: ${e.message}")
            ""
        }
    }


    override suspend fun datos_alumno(): String {
        return try {
            val res = snApiService.datos_alumno(bodyacceso.toRequestBody())
            val xmlResponse = res.string()
            Log.d("RXML", "XML Completo: $xmlResponse")

            if (xmlResponse.contains("<getAlumnoAcademicoWithLineamientoResult>")) {
                val resultado = xmlResponse
                    .substringAfter("<getAlumnoAcademicoWithLineamientoResult>")
                    .substringBefore("</getAlumnoAcademicoWithLineamientoResult>")

                Log.d("RXML", "Resultado extraído: $resultado")
                resultado
            } else {
                Log.e("RXML", "No se encontró la etiqueta accesoLoginResult")
                ""
            }
        } catch(e: Exception){
            Log.e("RXML", "Error en la peticion de datos: ${e.message}")
            ""
        }
    }

    suspend fun callHTTPS(){
        // Datos para la petición
        val matricula = "S22120240"
        val contrasenia = "W/w5xF3"
        val tipoUsuario = "ALUMNO" // o "DOCENTE", según corresponda

        // URL del servicio web SOAP
        val urlString = "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"

        // Cuerpo del mensaje SOAP
        val soapEnvelope = """
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <accesoLogin xmlns="http://tempuri.org/">
              <strMatricula>$matricula</strMatricula>
              <strContrasenia>$contrasenia</strContrasenia>
              <tipoUsuario>$tipoUsuario</tipoUsuario>
            </accesoLogin>
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

        try {
            // Establecer la conexión HTTPS
            val url = URL(urlString)
            val connection = url.openConnection() as HttpsURLConnection

            // Configurar la conexión
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Host", "sicenet.surguanajuato.tecnm.mx")
            connection.setRequestProperty("Content-Type", "text/xml; charset=\"UTF-8\"")
            //connection.setRequestProperty("Sec-Fetch-Mode", "cors")
            connection.setRequestProperty("Cookie", ".ASPXANONYMOUS=MaWJCZ-X2gEkAAAAODU2ZjkyM2EtNWE3ZC00NTdlLWFhYTAtYjk5ZTE5MDlkODIzeI1pCwvskL6aqtre4eT8Atfq2Po1;")
            connection.setRequestProperty("Content-Length", soapEnvelope.length.toString())
            connection.setRequestProperty("SOAPAction", "\"http://tempuri.org/accesoLogin\"")

            // Enviar el cuerpo del mensaje SOAP
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(soapEnvelope.toByteArray(Charsets.UTF_8))
            outputStream.close()

            // Leer la respuesta del servicio
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val cookies = connection.getHeaderField("Set-Cookie")
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                val response = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                // Aquí puedes procesar la respuesta del servicio
                println("Respuesta del servicio: $response")
                Log.d("SXML","Respuesta del servicio: $response")
            } else {
                // Manejar errores de conexión
                println("Error en la conexión: $responseCode")
            }

            // Cerrar la conexión
            connection.disconnect()
        } catch (e: IOException) {
            // Manejar excepciones de conexión
            e.printStackTrace()
        }
    }

}

