## README - Migración a SQLite en Juego Simon

### 1. **Nuevos Archivos Creados**

#### `RecordDBHelper.kt` - Clase Helper para SQLite
```kotlin
class RecordDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "SimonRecords.db"
        
        private const val SQL_CREATE_ENTRIES = """
            CREATE TABLE records (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                record INTEGER NOT NULL,
                fecha TEXT NOT NULL
            )
        """
    }
    
    // Métodos CRUD: insertRecord(), getBestRecord(), getAllRecords()
}
```

#### `ControllerSQLite.kt` - Controlador para SQLite
```kotlin
object ControllerSQLite : Conexion {
    override fun obtenerRecord(context: Context): Record {
        return dbHelper.getBestRecord()
    }
    
    override fun actualizarRecord(nuevoRecord: Int, fecha: Date, context: Context): Record {
        dbHelper.insertRecord(nuevoRecord, fecha)
        return Record(nuevoRecord, fecha)
    }
}
```

### 2. **Archivos Modificados**

#### `MyViewModel.kt` - ANTES (SharedPreferences)
```kotlin
init {
    recordp.value = Controller.obtenerRecord(getApplication()).record
}

private fun gameOver() {
    if (ronda.value > recordp.value) {
        recordp.value = ronda.value
        Controller.actualizarRecord(recordp.value, Date(), getApplication())
    }
}
```

#### `MyViewModel.kt` - DESPUÉS (SQLite)
```kotlin
init {
    cargarRecordInicial()
}

private fun cargarRecordInicial() {
    viewModelScope.launch {
        val record = ControllerSQLite.obtenerRecord(getApplication())
        recordp.value = record.record
    }
}

private fun gameOver() {
    val recordActual = ControllerSQLite.obtenerRecord(getApplication()).record
    if (ronda.value > recordActual) {
        recordp.value = ronda.value
        viewModelScope.launch {
            ControllerSQLite.actualizarRecord(ronda.value, Date(), getApplication())
        }
    }
}
```


## 📈 Ventajas de SQLite sobre SharedPreferences

| Aspecto | SharedPreferences | SQLite |
|---------|-------------------|---------|
| **Historial** | Solo último récord | Todos los récords |
| **Estructura** | Clave-valor simple | Tablas relacionales |
| **Consultas** | Muy limitadas | Complejas (ordenar, filtrar, etc.) |
| **Escalabilidad** | Baja | Alta |
| **Mantenimiento** | Simple | Más robusto |


## 🔄 **Cambios**

| Componente            | Estado        | 
|-----------------------|---------------|
| `RecordDBHelper.kt`   | ✅ Nuevo       | 
| `ControllerSQLite.kt` | ✅ Nuevo       | 
| `MyViewModel.kt`      | 🔄 Modificado |
| `IU`                  | ❌ Sin cambios |
| `MainActivity.kt`     | ❌ Sin cambios |
| `Controller.kt`       | Ya no se usa  |

