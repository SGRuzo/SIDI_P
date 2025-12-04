package com.SarayDani.sidi

import android.content.Context
import java.util.Date

interface Conexion {
    fun obtenerRecord(context: Context): Record
    fun actualizarRecord(nuevoRecord: Int, fecha: Date, context: Context): Record
}