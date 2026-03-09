package com.example.myapplication.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.myapplication.DB.AppDataBase
import com.example.myapplication.DB.Entidad.CargaAcademica
import com.example.myapplication.DB.Entidad.KardexItem

class MyContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.myapplication.provider"

        private const val KARDEX = 1
        private const val CARGA = 2

        private const val PERFIL = 3

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "kardex", KARDEX)
            addURI(AUTHORITY, "carga_academica", CARGA)
            addURI(AUTHORITY, "perfil", PERFIL)
        }
    }

    private lateinit var database: AppDataBase

    override fun onCreate(): Boolean {
        context?.let {
            database = AppDataBase.getDatabase(it)
            return true
        }
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            KARDEX -> {
                val cursor = database.kardexDao().getKardexCursor()
                cursor.setNotificationUri(context?.contentResolver, uri)
                cursor
            }
            CARGA -> {
                val cursor = database.cargaAcademicaDao().getCargaAcademicaCursor()
                cursor.setNotificationUri(context?.contentResolver, uri)
                cursor
            }
            PERFIL -> {
                val cursor = database.perfilDao().getPerfilCursor()
                cursor.setNotificationUri(context?.contentResolver, uri)
                cursor
            }
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            KARDEX -> "vnd.android.cursor.dir/$AUTHORITY.kardex"
            CARGA -> "vnd.android.cursor.dir/$AUTHORITY.carga_academica"
            PERFIL -> "vnd.android.cursor.dir/$AUTHORITY.perfil"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val resultId: Long = when (uriMatcher.match(uri)) {
            KARDEX -> {
                val item = mapToKardex(values)
                database.kardexDao().insertarKardexCursor(item)
            }
            CARGA -> {
                val item = mapToCarga(values)
                database.cargaAcademicaDao().insertarCargaAcademicaCursor(item)
            }
            else -> -1L
        }

        if (resultId >= 0) {
            context?.contentResolver?.notifyChange(uri, null)
            return Uri.withAppendedPath(uri, resultId.toString())
        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val count = when (uriMatcher.match(uri)) {
            KARDEX -> {
                val item = mapToKardex(null, selectionArgs)
                Log.d("ContentProvider", "Eliminacion de los datos $item")
                database.kardexDao().eliminarKardexCursor(item)
            }
            CARGA -> {
                val item = mapToCarga(null, selectionArgs)
                database.cargaAcademicaDao().eliminarCargaAcademica(item)
            }
            else -> 0
        }

        Log.d("ContentProvider", "Eliminacion de los datos")
        if (count > 0) context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val count = when (uriMatcher.match(uri)) {
            KARDEX -> {
                val item = mapToKardex(values)
                Log.d("ContentProvider", "Actualizacion de los datos $item")
                database.kardexDao().actualizarKardexCursor(item)
            }
            CARGA -> {
                val item = mapToCarga(values)
                Log.d("ContentProvider", "Actualizacion de los datos $item")
                database.cargaAcademicaDao().actualizarCargaAcademica(item)
            }
            else -> 0
        }
        Log.d("ContentProvider", "Actualizacion de los datos $count")
        if (count > 0) context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    // Funciones auxiliares para no repetir código de mapeo
    private fun mapToKardex(values: ContentValues?, selectionArgs: Array<out String>? = null): KardexItem {
        val id = values?.getAsInteger("id")
            ?: selectionArgs?.getOrNull(0)?.toIntOrNull()
            ?: 0

        return KardexItem(
            id = id,
            Materia = values?.getAsString("Materia") ?: selectionArgs?.getOrNull(0) ?: "",
            Calif = values?.getAsInteger("Calif") ?: 0,
            Cdts = values?.getAsInteger("Cdts") ?: 0,
            ClvMat = values?.getAsString("ClvMat") ?: "",
            ClvOfiMat = values?.getAsString("ClvOfiMat") ?: "",
            Acred = values?.getAsString("Acred") ?: ""
        )
    }

    private fun mapToCarga(values: ContentValues?, selectionArgs: Array<out String>? = null): CargaAcademica {
        val id = values?.getAsInteger("id")
            ?: selectionArgs?.getOrNull(0)?.toIntOrNull()
            ?: 0

        return CargaAcademica(
            id = id,
            Materia = values?.getAsString("Materia") ?: selectionArgs?.getOrNull(0) ?: "",
            Grupo = values?.getAsString("Grupo") ?: "",
            Docente = values?.getAsString("Docente") ?: "",
            CreditosMateria = values?.getAsInteger("CreditosMateria") ?: 0,
            EstadoMateria = values?.getAsString("EstadoMateria") ?: "",
            Sabado = values?.getAsString("Sabado") ?: "",
            Viernes = values?.getAsString("Viernes") ?: "",
            Jueves = values?.getAsString("Jueves") ?: "",
            Miercoles = values?.getAsString("Miercoles") ?: "",
            Martes = values?.getAsString("Martes") ?: "",
            Lunes = values?.getAsString("Lunes") ?: "",
            clvOficial = values?.getAsString("clvOficial") ?: "",
            Observaciones = values?.getAsString("Observaciones") ?: "",
            Semipresencial = values?.getAsString("Semipresencial") ?: ""
        )
    }
}
