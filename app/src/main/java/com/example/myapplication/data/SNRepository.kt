package com.example.marsphotos.data

import android.util.Log
import com.example.myapplication.DB.DAO.DaoCalificacionesFinal
import com.example.myapplication.DB.DAO.DaoCalificacionesUnidad
import com.example.myapplication.DB.DAO.DaoCargaAcademica
import com.example.myapplication.DB.DAO.DaoEstudiante
import com.example.myapplication.DB.DAO.DaoKardex
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
    suspend fun getKardex(): String
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

    override suspend fun getKardex(): String {
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
            val xmlResponse = res.string()
            if (xmlResponse.contains("<accesoLoginResult>")) {
                xmlResponse.substringAfter("<accesoLoginResult>").substringBefore("</accesoLoginResult>")
            } else ""
        } catch (e: Exception) { "" }
    }

    override suspend fun datos_alumno(): String {
        return try {
            val res = snApiService.datos_alumno(bodyPerfil.toRequestBody())
            val xmlResponse = res.string()
            if (xmlResponse.contains("<getAlumnoAcademicoWithLineamientoResult>")) {
                xmlResponse.substringAfter("<getAlumnoAcademicoWithLineamientoResult>").substringBefore("</getAlumnoAcademicoWithLineamientoResult>")
            } else ""
        } catch (e: Exception) { "" }
    }

    override suspend fun getCargaAcademica(): String {
        return try {
            val res = snApiService.getCargaAcademica(bodyacceso.toRequestBody())
            val xmlResponse = res.string()
            if (xmlResponse.contains("<getCargaAcademicaByAlumnoResult>")) {
                xmlResponse.substringAfter("<getCargaAcademicaByAlumnoResult>").substringBefore("</getCargaAcademicaByAlumnoResult>")
            } else ""
        } catch (e: Exception) { "" }
    }

    override suspend fun getKardex(): String {
        return try {
            val res = snApiService.getKardex(bodyacceso.toRequestBody())
            val xmlResponse = res.string()
            if (xmlResponse.contains("<getAllKardexConPromedioByAlumnoResult>")) {
                xmlResponse.substringAfter("<getAllKardexConPromedioByAlumnoResult>").substringBefore("</getAllKardexConPromedioByAlumnoResult>")
            } else ""
        } catch (e: Exception) { "" }
    }

    override suspend fun getCalificacionesUnidad(): String {
        return try {
            val res = snApiService.getCalificacionesUnidad(bodyacceso.toRequestBody())
            val xmlResponse = res.string()
            if (xmlResponse.contains("<getCalifUnidadesByAlumnoResult>")) {
                xmlResponse.substringAfter("<getCalifUnidadesByAlumnoResult>").substringBefore("</getCalifUnidadesByAlumnoResult>")
            } else ""
        } catch (e: Exception) { "" }
    }

    override suspend fun getCalificacionesFinales(): String {
        return try {
            val res = snApiService.getCalificacionesFinales(bodyacceso.toRequestBody())
            val xmlResponse = res.string()
            if (xmlResponse.contains("<getAllCalifFinalByAlumnosResult>")) {
                xmlResponse.substringAfter("<getAllCalifFinalByAlumnosResult>").substringBefore("</getAllCalifFinalByAlumnosResult>")
            } else ""
        } catch (e: Exception) { "" }
    }
}
