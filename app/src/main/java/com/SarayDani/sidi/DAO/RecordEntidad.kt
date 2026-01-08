package com.SarayDani.sidi.DAO

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_table")
data class RecordEntidad(
    @PrimaryKey val id: Int = 1, // Usamos un ID fijo porque solo guardamos un récord global
    val record: Int,
    val fecha: Long // Guardamos la fecha como Long (timestamp)
)