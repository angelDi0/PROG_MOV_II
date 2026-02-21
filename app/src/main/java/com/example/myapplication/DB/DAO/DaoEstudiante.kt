package com.example.myapplication.DB.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.DB.Entidad.Estudiante
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoEstudiante {

    @Query("SELECT * FROM perfil_estudiante LIMIT 1")
    fun getPerfil(): Flow<Estudiante>

    @Query("SELECT * FROM perfil_estudiante LIMIT 1")
    suspend fun getPerfilSync(): Estudiante?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDatosPerfil(perfil: Estudiante)
}