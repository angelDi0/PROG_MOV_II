package com.example.myapplication.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.myapplication.DB.AppDataBase

class MyContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.myapplication.provider"

        private const val KARDEX = 1
        private const val CARGA = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "kardex", KARDEX)
            addURI(AUTHORITY, "carga_academica", CARGA)
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
        // Usamos los DAOs para obtener el Cursor directamente
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
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            KARDEX -> "vnd.android.cursor.dir/$AUTHORITY.kardex"
            CARGA -> "vnd.android.cursor.dir/$AUTHORITY.carga_academica"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
