package com.example.myapplication.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.DB.DAO.DaoCalificacionesFinal
import com.example.myapplication.DB.DAO.DaoCalificacionesUnidad
import com.example.myapplication.DB.DAO.DaoCargaAcademica
import com.example.myapplication.DB.DAO.DaoEstudiante
import com.example.myapplication.DB.DAO.DaoKardex
import com.example.myapplication.DB.Entidad.CalificacionFinalItem
import com.example.myapplication.DB.Entidad.CalificacionesUnidadItem
import com.example.myapplication.DB.Entidad.CargaAcademica
import com.example.myapplication.DB.Entidad.Estudiante
import com.example.myapplication.DB.Entidad.KardexItem

@Database(entities = [Estudiante::class, CargaAcademica::class, KardexItem::class, CalificacionesUnidadItem::class, CalificacionFinalItem::class],
    version = 5, exportSchema = false)
abstract class AppDataBase : RoomDatabase(){
    abstract fun perfilDao(): DaoEstudiante
    abstract fun cargaAcademicaDao(): DaoCargaAcademica
    abstract fun kardexDao(): DaoKardex
    abstract fun calificacionesUnidadDao(): DaoCalificacionesUnidad
    abstract fun calificacionesFinalDao(): DaoCalificacionesFinal


    companion object {
        @Volatile
        private var Instance: AppDataBase? = null

        fun getDatabase(context: Context):AppDataBase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDataBase::class.java, "sicenet_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it}
            }
        }
    }
}