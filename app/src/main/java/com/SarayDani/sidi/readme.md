# USO ROOM EN SIMON DICE
## Configuración del Proyecto (`build.gradle.kts`)

1. **Plugin KAPT:** Se habilitó `kotlin("kapt")` para permitir el procesamiento de Room.
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt") // Plugin KAPT
}
```

2. **Dependencias:**
* `room-runtime`: Librería base para el funcionamiento de la base de datos.
* `room-compiler`: Procesador de anotaciones necesario para la compilación.

```kotlin
val room_version = "2.8.4"
implementation("androidx.room:room-runtime:$room_version")
kapt("androidx.room:room-compiler:$room_version")
```


---

## Arquitectura de Persistencia

La implementación se divide en tres componentes principales siguiendo el patrón recomendado por Android:

### 1. La Entidad: `RecordEntidad.kt`

Define la estructura de la tabla `record_table`.

* **Decisión - ID Fijo:** Se utiliza `@PrimaryKey val id: Int = 1`. Al ser un juego de un solo jugador local, solo necesitamos almacenar un único registro global. Forzar el ID a 1 asegura que siempre estemos actualizando el mismo dato en lugar de crear una lista de puntuaciones.
* **Fecha como Long:** Room no almacena objetos `Date` nativamente. Se guarda como `Long` (timestamp) por eficiencia y facilidad de conversión.


```kotlin
@Entity(tableName = "record_table")
data class RecordEntidad(
    @PrimaryKey val id: Int = 1, // Usamos un ID fijo porque solo guardamos un récord global
    val record: Int,
    val fecha: Long // Guardamos la fecha como Long (timestamp)
)
```

### 2. El Acceso a Datos: `RecordDAO.kt`

Define los métodos para interactuar con la base de datos.

* **`obtenerRecord()`**: Devuelve un objeto nullable (`RecordEntidad?`) porque en el primer inicio del juego la base de datos estará vacía.
* **`OnConflictStrategy.REPLACE`**: Se utiliza en la inserción para que, al intentar guardar un nuevo récord con el mismo ID (ID=1), Room sobrescriba automáticamente el anterior.

```kotlin
@Dao
interface RecordDAO {
    @Query("SELECT * FROM record_table WHERE id = 1")
    fun obtenerRecord(): RecordEntidad?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarRecord(record: RecordEntidad)
}
```

### 3. La Base de Datos: `AppDatabase.kt`

Actúa como el punto de acceso principal.

* **Patrón Singleton:** Se implementó mediante un `companion object` con `@Volatile` para asegurar que solo exista una instancia de la base de datos en toda la aplicación, evitando fugas de memoria o conflictos de escritura.
* **`allowMainThreadQueries()`**: **Decisión de diseño:** Aunque en aplicaciones de producción se recomienda usar corrutinas (`suspend functions`) para no bloquear la interfaz, aquí se habilitó para simplificar el flujo lógico en un entorno educativo/pequeño donde el volumen de datos es mínimo y no impacta el rendimiento.

```kotlin

@Database(entities = [RecordEntidad::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sidi_database"
                )
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## Integración en la Lógica de Negocio (`MyViewModel.kt`)

El `ViewModel` actúa como puente entre la UI y Room:

1. **Carga Inicial:** En el bloque `init`, se llama a `cargarRecordInicial()`. Esto garantiza que, nada más abrir la app, el usuario vea su récord actual recuperado de la memoria persistente.
```kotlin
   private val recordDao = AppDatabase.getDatabase(application).recordDao()
```
2. **Lógica de Guardado en `gameOver()**`:
* Antes de guardar, el ViewModel consulta el récord actual almacenado.
* Solo si la ronda actual supera el récord existente, se procede a realizar un `guardarRecord()`.
* Esto optimiza las operaciones de escritura, realizándolas solo cuando es estrictamente necesario.
```kotlin
private fun cargarRecordInicial() {
    val entidad = recordDao.obtenerRecord()
    recordp.value = entidad?.record ?: 0
    Log.d(TAG_LOG, "Record cargado de Room: ${recordp.value}")
}

private fun gameOver() {
    estadoActual.value = Estados.GameOver

    // Consulta y guarda de forma directa
    val recordExistente = recordDao.obtenerRecord()?.record ?: 0

    if (ronda.value > recordExistente) {
        recordp.value = ronda.value
        recordDao.guardarRecord(
            RecordEntidad(record = ronda.value, fecha = System.currentTimeMillis())
        )
        Log.d(TAG_LOG, "Nuevo récord guardado con ROOM")
    }
    //  Limpieza de UI
    botonEncendido.value = null
    Log.d(TAG_LOG, "GAME OVER. Ronda alcanzada: ${ronda.value}")
}
   ```


---

## Decisiones

| Decisión                             | ¿Por qué lo hicimos así? |
|--------------------------------------| --- |
| **Lo bueno de ROOM** | Si en el futuro quieres guardar una lista de los 10 mejores jugadores con sus nombres, ya tienes el sistema preparado para crecer fácilmente.
|
| **¿Por qué ID = 1?**  | Como este juego solo tiene **un récord**, no necesitamos una lista infinita de datos. Usar siempre el número 1 es como tener un único "cajón" donde siempre guardamos y actualizamos el mismo número.
|
| **¿Por qué `allowMainThreadQueries`?** | Normalmente, las bases de datos son lentas y pueden congelar la pantalla. Pero como aquí solo guardamos **un solo número**, lo activamos para que el código sea mucho más corto y fácil de entender sin que el móvil se trabe. |

