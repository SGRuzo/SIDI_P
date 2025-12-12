package com.SarayDani.sidi

import android.content.Context
import java.util.Date

object ControllerSQLite : Conexion {

    private var dbHelper: RecordDbHelper? = null


    /**
     * Comprueba si dbHelper es nulo.
     * Si es nulo crea RecordDbHelper con context.applicationContext
     */
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
        dbHelper.insertRecord(nuevoRecord, fecha)// Insertar nuevo récord
        return Record(nuevoRecord, fecha)
    }

}