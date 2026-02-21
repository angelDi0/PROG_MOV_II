package com.example.myapplication.DB.Entidad

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "perfil_estudiante")
@Serializable
data class Estudiante(
    @PrimaryKey() val matricula: String,
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

    // campo para saber cuando fue la ultima vez que se actualizo
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "carga_academica")
@Serializable
data class CargaAcademica(
    @PrimaryKey val index: Int = 0,
    val Semipresencial: String = "",
    val Observaciones: String = "",
    val Docente: String = "",
    val clvOficial: String = "",
    val Sabado: String = "",
    val Viernes: String = "",
    val Jueves: String = "",
    val Miercoles: String = "",
    val Martes: String = "",
    val Lunes: String = "",
    val EstadoMateria: String = "",
    val CreditosMateria: Int = 0,
    val Materia: String = "",
    val Grupo: String = "",
)

@Entity(tableName = "kardex")
@Serializable
data class KardexItem(
    @PrimaryKey val index: Int = 0,
    val S3: String = "",
    val P3: String = "",
    val A3: String = "",
    val ClvMat: String = "",
    val ClvOfiMat: String = "",
    val Materia: String = "",
    val Cdts: Int = 0,
    val Calif: Int = 0,
    val Acred: String = "",
    val S1: String = "",
    val P1: String = "",
    val A1: String = "",
    val S2: String = "",
    val P2: String = "",
    val A2: String = "",
)

@Entity(tableName = "calificaciones_unidad")
@Serializable
data class CalificacionesUnidadItem(
    @PrimaryKey val index: Int = 0,
    val Observaciones: String = "",
    val C13: String = "",
    val C12: String = "",
    val C11: String = "",
    val C10: String = "",
    val C9: String = "",
    val C8: String = "",
    val C7: String = "",
    val C6: String = "",
    val C5: String = "",
    val C4: String = "",
    val C3: String = "",
    val C2: String = "",
    val C1: String = "",
    val UnidadesActivas: String = "",
    val Materia: String = "",
    val Grupo: String = "",
)

@Entity(tableName = "calificacion_final")
@Serializable
data class CalificacionFinalItem(
    @PrimaryKey val index: Int = 0,
    val calif: Int = 0,
    val acred: String = "",
    val grupo: String = "",
    val materia: String = "",
    val Observaciones: String = "",
)
