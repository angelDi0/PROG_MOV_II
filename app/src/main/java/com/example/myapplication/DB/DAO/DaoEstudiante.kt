package com.example.myapplication.DB.DAO

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.DB.Entidad.CalificacionFinalItem
import com.example.myapplication.DB.Entidad.CalificacionesUnidadItem
import com.example.myapplication.DB.Entidad.CargaAcademica
import com.example.myapplication.DB.Entidad.Estudiante
import com.example.myapplication.DB.Entidad.KardexItem

@Dao
interface DaoEstudiante {
    @Query("SELECT * FROM perfil_estudiante LIMIT 1")
    suspend fun getPerfilSync(): Estudiante?

    @Query("SELECT * FROM perfil_estudiante LIMIT 1")
    fun getPerfilCursor(): Cursor


    @Query("SELECT * FROM perfil_estudiante WHERE matricula = :matricula")
    suspend fun getPerfil(matricula: String): Estudiante?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDatosPerfil(perfil: Estudiante): Long
}

@Dao
interface DaoCargaAcademica {
    @Query("SELECT * FROM carga_academica")
    suspend fun getCargaAcademicaSync(): List<CargaAcademica>

    @Query("SELECT * FROM carga_academica")
    fun getCargaAcademicaCursor(): Cursor
    
    @Update
    fun actualizarCargaAcademica(carga: CargaAcademica): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCargaAcademica(carga: CargaAcademica): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarCargaAcademicaCursor(carga: CargaAcademica): Long

    @Delete
    fun eliminarCargaAcademica(carga: CargaAcademica): Int

    @Query("DELETE from carga_academica")
    suspend fun eliminarTodo()
}

@Dao
interface DaoKardex {
    @Query("SELECT * FROM kardex")
    suspend fun getKardexSync(): List<KardexItem>

    @Query("SELECT * FROM kardex")
    fun getKardexCursor(): Cursor

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarKardex(karde: KardexItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarKardexCursor(kardex: KardexItem): Long

    @Update
    fun actualizarKardexCursor(kardex: KardexItem): Int

    @Delete
    fun eliminarKardexCursor(kardex: KardexItem): Int

    @Query("DELETE FROM kardex")
    suspend fun eliminarTodo()
}

@Dao
interface DaoCalificacionesUnidad{
    @Query("SELECT * FROM calificaciones_unidad")
    suspend fun getCalificacionesUnidadSync(): List<CalificacionesUnidadItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCalificacionesUnidad(calificaciones: CalificacionesUnidadItem): Long

    @Query("DELETE from calificaciones_unidad")
    suspend fun eliminarTodo()
}

@Dao
interface DaoCalificacionesFinal{
    @Query("SELECT * FROM calificacion_final")
    suspend fun getCalificacionesFinalesSync(): List<CalificacionFinalItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCalificacionFinal(calificacionfinal: CalificacionFinalItem): Long

    @Query("DELETE from calificacion_final")
    suspend fun eliminarTodo()
}
