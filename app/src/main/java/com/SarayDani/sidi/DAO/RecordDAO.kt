package com.SarayDani.sidi.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordDAO {
    @Query("SELECT * FROM record_table WHERE id = 1")
    fun obtenerRecord(): RecordEntidad?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarRecord(record: RecordEntidad)
}