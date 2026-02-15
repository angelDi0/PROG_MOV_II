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
    val nombre: String = ""
)