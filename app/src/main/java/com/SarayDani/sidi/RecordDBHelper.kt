package com.SarayDani.sidi

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * recoge los datos de la tabla
 */
object RecordContract {
    object RecordEntry : BaseColumns {
        const val TABLE_NAME = "records"
        const val COLUMN_RECORD = "record"
        const val COLUMN_FECHA = "fecha"
    }
}

class RecordDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val TAG_LOG = "RecordDbHelper"
    private val FORMATO_FECHA = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "SimonRecords.db"

        // para crear tabla
        private const val SQL_CREATE_ENTRIES = """
            CREATE TABLE ${RecordContract.RecordEntry.TABLE_NAME} (
                ${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${RecordContract.RecordEntry.COLUMN_RECORD} INTEGER NOT NULL,
                ${RecordContract.RecordEntry.COLUMN_FECHA} TEXT NOT NULL
            )
        """

        // para elimnar tabla
        private const val SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS ${RecordContract.RecordEntry.TABLE_NAME}"
    }

    // Crear tabla
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        Log.d(TAG_LOG, "Tabla 'records' creada")

        // Insertar registro inicial con récord 0
        insertRecord(db, 0, Date())
        Log.d(TAG_LOG, "Registro inicial insertado")
    }

    // Actualizar tabla
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        Log.d(TAG_LOG, "Tabla 'records' eliminada por upgrade")
        onCreate(db)
    }

    // Eliminar tabla
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }


    /**
     * funcion publica para insertar un nuevo récord
     */
    fun insertRecord(record: Int, fecha: Date): Long {
        val db = writableDatabase
        return insertRecord(db, record, fecha)
    }

    /**
     * Declara una función privada que recibe una instancia de SQLiteDatabase, un entero record y una Date.
     * Devuelve un Long que es el ID de la fila insertada o -1 si falla.
     */
    private fun insertRecord(db: SQLiteDatabase, record: Int, fecha: Date): Long {
        val values = ContentValues().apply {
            put(RecordContract.RecordEntry.COLUMN_RECORD, record)
            put(RecordContract.RecordEntry.COLUMN_FECHA, FORMATO_FECHA.format(fecha))
        }
        val newRowId = db.insertWithOnConflict(
            RecordContract.RecordEntry.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
        if (newRowId != -1L) {
            Log.d(TAG_LOG, "Record insertado con ID: $newRowId")
        } else {
            Log.e(TAG_LOG, "Error insertando record")
        }
        return newRowId
    }

    /**
     * Obtiene el mejor récord (el más alto)
     */
    fun getBestRecord(): Record {
        val db = readableDatabase

        // Consulta para obtener el mejor récord
        val projection = arrayOf(
            RecordContract.RecordEntry.COLUMN_RECORD,
            RecordContract.RecordEntry.COLUMN_FECHA
        )

        // Ordenar por el campo "record" en orden descendente y limitar a 1
        val cursor: Cursor = db.query(
            RecordContract.RecordEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            "${RecordContract.RecordEntry.COLUMN_RECORD} DESC",
            "1"
        )

        // Procesar el cursor para obtener el mejor récord
        return try {
            if (cursor.moveToFirst()) {
                val record = cursor.getInt(cursor.getColumnIndexOrThrow(RecordContract.RecordEntry.COLUMN_RECORD))
                val fechaString = cursor.getString(cursor.getColumnIndexOrThrow(RecordContract.RecordEntry.COLUMN_FECHA))

                // Convertir la cadena de fecha a un objeto Date
                val fecha = try {
                    FORMATO_FECHA.parse(fechaString) ?: Date()
                } catch (e: Exception) {
                    Date()
                }
                Record(record, fecha)
            } else {
                Log.d(TAG_LOG, "No hay records, devolviendo valor por defecto")
                Record(0, Date())
            }
        } finally {
            cursor.close()
        }
    }

}