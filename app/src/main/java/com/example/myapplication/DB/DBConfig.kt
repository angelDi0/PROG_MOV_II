package com.example.myapplication.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.DB.DAO.DaoEstudiante
import com.example.myapplication.DB.Entidad.Estudiante

@Database(entities = [Estudiante::class], version = 1, exportSchema = false)
abstract class AppDataBase : RoomDatabase(){
    abstract fun perfilDao(): DaoEstudiante


    companion object {
        @Volatile
        private var Instance: AppDataBase? = null

        fun getDatabase(context: Context):AppDataBase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDataBase::class.java, "sicenet_db")
                    .build()
                    .also { Instance = it}
            }
        }
    }
}