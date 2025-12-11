package com.SarayDani.sidi

import android.content.Context
import java.util.Date

object ControllerSQLite : Conexion {

    private var dbHelper: RecordDbHelper? = null

    private fun getDbHelper(context: Context): RecordDbHelper {
        if (dbHelper == null) {
            dbHelper = RecordDbHelper(context.applicationContext)
        }
        return dbHelper!!
    }

    override fun obtenerRecord(context: Context): Record {
        val dbHelper = getDbHelper(context)
        return dbHelper.getBestRecord()
    }

    override fun actualizarRecord(nuevoRecord: Int, fecha: Date, context: Context): Record {
        val dbHelper = getDbHelper(context)

        // Insertar nuevo récord
        dbHelper.insertRecord(nuevoRecord, fecha)

        // Opcional: Mantener solo los 10 mejores
        dbHelper.keepTopRecords(10)

        return Record(nuevoRecord, fecha)
    }

    /**
     * Método adicional: Obtener todos los récords
     */
    fun obtenerTodosLosRecords(context: Context): List<Record> {
        val dbHelper = getDbHelper(context)
        return dbHelper.getAllRecords()
    }

    /**
     * Método adicional: Obtener estadísticas
     */
    fun obtenerEstadisticas(context: Context): RecordDbHelper.DatabaseStats {
        val dbHelper = getDbHelper(context)
        return dbHelper.getDatabaseStats()
    }

    /**
     * Método adicional: Resetear base de datos
     */
    fun resetDatabase(context: Context) {
        val dbHelper = getDbHelper(context)
        dbHelper.deleteAllRecords()

        // Insertar registro inicial
        dbHelper.insertRecord(0, Date())
    }
}