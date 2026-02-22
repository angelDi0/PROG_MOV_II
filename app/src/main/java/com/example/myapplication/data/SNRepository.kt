package com.example.marsphotos.data

import android.util.Log
import com.example.myapplication.DB.DAO.DaoCalificacionesFinal
import com.example.myapplication.DB.DAO.DaoCalificacionesUnidad
import com.example.myapplication.DB.DAO.DaoCargaAcademica
import com.example.myapplication.DB.DAO.DaoEstudiante
import com.example.myapplication.DB.DAO.DaoKardex
import com.example.myapplication.DB.Entidad.Estudiante
import com.example.myapplication.network.KardexItem
import com.example.myapplication.network.SICENETWService
import com.example.myapplication.network.bodyPerfil
import com.example.myapplication.network.bodyacceso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Repository interface
 */
interface SNRepository {
    suspend fun acceso(m: String, p: String): String
    suspend fun datos_alumno(): String
    suspend fun getCargaAcademica(): String
    suspend fun getKardex(l: Int = 0): String
    suspend fun getCalificacionesUnidad(): String
    suspend fun getCalificacionesFinales(): String
}

/**
 * Implementation for Local DB
 */
class DBLocalSNRepository(
    private val daoEstudiante: DaoEstudiante,
    private val daoKardex: DaoKardex,
    private val daoCarga: DaoCargaAcademica,
    private val daoUnidad: DaoCalificacionesUnidad,
    private val daoFinal: DaoCalificacionesFinal
) : SNRepository {

    override suspend fun acceso(m: String, p: String): String = "SUCCESS"

    override suspend fun datos_alumno(): String {
        val estudiante = daoEstudiante.getPerfilSync()
        return if (estudiante != null) Json.encodeToString(estudiante) else ""
    }

    override suspend fun getCargaAcademica(): String {
        val data = daoCarga.getCargaAcademicaSync()
        return Json.encodeToString(data)
    }

    override suspend fun getKardex(l: Int): String {
        val data = daoKardex.getKardexSync()
        return Json.encodeToString(data)
    }

    override suspend fun getCalificacionesUnidad(): String {
        val data = daoUnidad.getCalificacionesUnidadSync()
        return Json.encodeToString(data)
    }

    override suspend fun getCalificacionesFinales(): String {
        val data = daoFinal.getCalificacionesFinalesSync()
        return Json.encodeToString(data)
    }
}

/**
 * Implementation for Network
 */
class NetworSNRepository(
    private val snApiService: SICENETWService,
) : SNRepository {
    override suspend fun acceso(m: String, p: String): String {
        return try {
            val res = snApiService.acceso(bodyacceso.format(m, p).toRequestBody())
            val xmlString = res.string()
            if (xmlString.contains("<accesoLoginResult>")) {
                xmlString.substringAfter("<accesoLoginResult>").substringBefore("</accesoLoginResult>")
            } else ""
        } catch (e: Exception) { "" }
    }

    override suspend fun datos_alumno(): String {
        return try {
            val res = snApiService.datos_alumno(bodyPerfil.toRequestBody())
            val xmlString = res.string()
            if (xmlString.contains("<getAlumnoAcademicoWithLineamientoResult>")) {
                xmlString.substringAfter("<getAlumnoAcademicoWithLineamientoResult>").substringBefore("</getAlumnoAcademicoWithLineamientoResult>")
            } else ""
        } catch (e: Exception) { "" }
    }

    override suspend fun getCargaAcademica(): String {
        return try {
            val res = snApiService.getCargaAcademica(bodyacceso.toRequestBody())
            val xmlString = res.string()
            if (xmlString.contains("<getCargaAcademicaByAlumnoResult>")) {
                xmlString.substringAfter("<getCargaAcademicaByAlumnoResult>").substringBefore("</getCargaAcademicaByAlumnoResult>")
            } else ""
        } catch (e: Exception) { "" }
    }

    override suspend fun getKardex(l: Int): String {
        return try {
            Log.d("KAR", "LINEMAMIENTO: $l")
            val res = snApiService.getKardex(KardexItem.format(l).toRequestBody())
            val xmlString = res.string() 
            Log.d("KAR", "Respuesta recibida: $xmlString")
            
            if (xmlString.contains("<getAllKardexConPromedioByAlumnoResult>")) {
                xmlString.substringAfter("<getAllKardexConPromedioByAlumnoResult>").substringBefore("</getAllKardexConPromedioByAlumnoResult>")
            } else ""
        } catch (e: Exception) { 
            Log.e("KAR", "Error en getKardex", e)
            "" 
        }
    }

    override suspend fun getCalificacionesUnidad(): String {
        return try {
            val res = snApiService.getCalificacionesUnidad(bodyacceso.toRequestBody())
            val xmlString = res.string()
            if (xmlString.contains("<getCalifUnidadesByAlumnoResult>")) {
                xmlString.substringAfter("<getCalifUnidadesByAlumnoResult>").substringBefore("</getCalifUnidadesByAlumnoResult>")
            } else ""
        } catch (e: Exception) { "" }
    }

    override suspend fun getCalificacionesFinales(): String {
        return try {
            val res = snApiService.getCalificacionesFinales(bodyacceso.toRequestBody())
            val xmlString = res.string()
            if (xmlString.contains("<getAllCalifFinalByAlumnosResult>")) {
                xmlString.substringAfter("<getAllCalifFinalByAlumnosResult>").substringBefore("</getAllCalifFinalByAlumnosResult>")
            } else ""
        } catch (e: Exception) { "" }
    }
}
