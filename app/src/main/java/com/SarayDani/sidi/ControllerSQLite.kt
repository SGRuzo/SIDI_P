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

        return Record(nuevoRecord, fecha)
    }

}