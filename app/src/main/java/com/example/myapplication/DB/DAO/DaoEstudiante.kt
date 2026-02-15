package com.example.myapplication.DB.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.DB.Entidad.Estudiante
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoEstudiante {

    @Query("SELECT * FROM perfil_estudiante WHERE matricula = :matricula")
    fun getPerfil(matricula: String): Flow<Estudiante>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarDatosPerfil(perfil: Estudiante): Estudiante?
}