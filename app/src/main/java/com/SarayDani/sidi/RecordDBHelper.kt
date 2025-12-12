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

        private const val SQL_CREATE_ENTRIES = """
            CREATE TABLE ${RecordContract.RecordEntry.TABLE_NAME} (
                ${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${RecordContract.RecordEntry.COLUMN_RECORD} INTEGER NOT NULL,
                ${RecordContract.RecordEntry.COLUMN_FECHA} TEXT NOT NULL
            )
        """

        private const val SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS ${RecordContract.RecordEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        Log.d(TAG_LOG, "Tabla 'records' creada")

        // Insertar registro inicial con récord 0
        insertRecord(db, 0, Date())
        Log.d(TAG_LOG, "Registro inicial insertado")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        Log.d(TAG_LOG, "Tabla 'records' eliminada por upgrade")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    // ============ OPERACIONES CRUD ============

    /**
     * CREATE: Inserta un nuevo récord
     */
    fun insertRecord(record: Int, fecha: Date): Long {
        val db = writableDatabase
        return insertRecord(db, record, fecha)
    }

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
     * READ: Obtiene el mejor récord (el más alto)
     */
    fun getBestRecord(): Record {
        val db = readableDatabase

        val projection = arrayOf(
            RecordContract.RecordEntry.COLUMN_RECORD,
            RecordContract.RecordEntry.COLUMN_FECHA
        )

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

        return try {
            if (cursor.moveToFirst()) {
                val record = cursor.getInt(cursor.getColumnIndexOrThrow(RecordContract.RecordEntry.COLUMN_RECORD))
                val fechaString = cursor.getString(cursor.getColumnIndexOrThrow(RecordContract.RecordEntry.COLUMN_FECHA))

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

    /**
     * READ: Obtiene todos los récords ordenados
     */
    fun getAllRecords(): List<Record> {
        val db = readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            RecordContract.RecordEntry.COLUMN_RECORD,
            RecordContract.RecordEntry.COLUMN_FECHA
        )

        val cursor: Cursor = db.query(
            RecordContract.RecordEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            "${RecordContract.RecordEntry.COLUMN_RECORD} DESC, ${RecordContract.RecordEntry.COLUMN_FECHA} DESC"
        )

        val records = mutableListOf<Record>()

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val record = getInt(getColumnIndexOrThrow(RecordContract.RecordEntry.COLUMN_RECORD))
                val fechaString = getString(getColumnIndexOrThrow(RecordContract.RecordEntry.COLUMN_FECHA))

                val fecha = try {
                    FORMATO_FECHA.parse(fechaString) ?: Date()
                } catch (e: Exception) {
                    Date()
                }

                records.add(Record(record, fecha))
            }
        }

        cursor.close()
        return records
    }



    /**
     * Metodo de utilidad: Obtiene estadísticas de la base de datos
     */
    fun getDatabaseStats(): DatabaseStats {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT COUNT(*) as count, MAX(${RecordContract.RecordEntry.COLUMN_RECORD}) as max_record FROM ${RecordContract.RecordEntry.TABLE_NAME}",
            null
        )

        return if (cursor.moveToFirst()) {
            val count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
            val maxRecord = cursor.getInt(cursor.getColumnIndexOrThrow("max_record"))
            cursor.close()
            DatabaseStats(count, maxRecord)
        } else {
            cursor.close()
            DatabaseStats(0, 0)
        }
    }

    /**
     * Data class para estadísticas
     */
    data class DatabaseStats(val totalRecords: Int, val bestRecord: Int)
}