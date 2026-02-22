package com.example.myapplication.DB.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.DB.Entidad.CalificacionFinalItem
import com.example.myapplication.DB.Entidad.CalificacionesUnidadItem
import com.example.myapplication.DB.Entidad.CargaAcademica
import com.example.myapplication.DB.Entidad.Estudiante
import com.example.myapplication.DB.Entidad.KardexItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoEstudiante {
    @Query("SELECT * FROM perfil_estudiante LIMIT 1")
    suspend fun getPerfilSync(): Estudiante?

    @Query("SELECT * FROM perfil_estudiante WHERE matricula = :matricula")
    suspend fun getPerfil(matricula: String): Estudiante?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDatosPerfil(perfil: Estudiante)
}

@Dao
interface DaoCargaAcademica {
    @Query("SELECT * FROM carga_academica")
    suspend fun getCargaAcademicaSync(): List<CargaAcademica>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCargaAcademica(carga: CargaAcademica)
}

@Dao
interface DaoKardex {
    @Query("SELECT * FROM kardex")
    suspend fun getKardexSync(): List<KardexItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarKardex(karde: KardexItem)
}

@Dao
interface DaoCalificacionesUnidad{
    @Query("SELECT * FROM calificaciones_unidad")
    suspend fun getCalificacionesUnidadSync(): List<CalificacionesUnidadItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCalificacionesUnidad(calificaciones: CalificacionesUnidadItem)
}

@Dao
interface DaoCalificacionesFinal{
    @Query("SELECT * FROM calificacion_final")
    suspend fun getCalificacionesFinalesSync(): List<CalificacionFinalItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCalificacionFinal(calificacionfinal: CalificacionFinalItem)
}
