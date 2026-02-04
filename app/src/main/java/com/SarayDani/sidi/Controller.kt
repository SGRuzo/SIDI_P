package com.SarayDani.sidi

import android.content.Context
import androidx.core.content.edit
import java.text.SimpleDateFormat // Importado
import java.util.Date
import java.util.Locale             // Importado

object Controller : Conexion {

    private const val PREFS_NAME = "preferencias_app"

    // Vuelve a ser un objeto SimpleDateFormat para poder usar .parse() y .format()
    private val FORMATO_FECHA = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)

    private const val KEY_RECORD = "record"
    private const val KEY_FECHA = "fecha"


    override fun obtenerRecord(context: Context): Record {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val recordValue: Int = sharedPreferences.getInt(KEY_RECORD, 0)
        val fechaString: String? = sharedPreferences.getString(KEY_FECHA, null)

        val fechaFormateada: Date = if (fechaString != null) {
            try {
                // .parse() de nuestro formateador
                FORMATO_FECHA.parse(fechaString) ?: Date()
            } catch (e: Exception) {
                Date() // Si hay error, devuelve la fecha actual
            }
        } else {
            Date() // Si no hay fecha, devuelve la actual
        }

        return Record(recordValue, fechaFormateada)
    }

    override fun actualizarRecord(nuevoRecord: Int, fecha: Date, context: Context): Record {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stringData = FORMATO_FECHA.format(fecha)

        sharedPreferences.edit {
            putInt(KEY_RECORD, nuevoRecord)
            putString(KEY_FECHA, stringData)
        }
        return Record(nuevoRecord, fecha)
    }

}