---
objetivo: Integrar persistencia de records en MongoDB al juego Simon Dice, manteniendo Shared Preferences y SQLite
version: 1.0
fecha_creacion: 2026-01-19
ultima_actualizacion: 2026-01-19
responsable: Equipo de Desarrollo SIDI
estado: 'Planificado'
etiquetas: ['funcionalidad', 'persistencia', 'mongodb', 'arquitectura']
---

# Introducción

![Estado: Planificado](https://img.shields.io/badge/estado-Planificado-blue)

Este plan de implementación describe los pasos necesarios para integrar **MongoDB** como tercera opción de persistencia de datos en la aplicación Simon Dice, complementando el almacenamiento existente en **Shared Preferences** y **SQLite (Room)**.

El objetivo es permitir que los records del juego se guarden, sincronicen y recuperen desde una base de datos remota MongoDB, manteniendo la compatibilidad con los sistemas de almacenamiento actual y respetando la arquitectura basada en la interfaz `Conexion`.

---

## 1. Requisitos y Restricciones

### Requisitos Funcionales

- **REQ-001**: La aplicación debe poder guardar records en MongoDB sin perder funcionalidad con Shared Preferences y SQLite
- **REQ-002**: Implementar patrón offline-first: si no hay conexión a Internet, se almacena localmente y se sincroniza cuando esté disponible
- **REQ-003**: La interfaz `Conexion` debe extenderse o adaptarse para soportar operaciones asincrónicas con MongoDB
- **REQ-004**: Los records debe contener: ID único, puntuación, fecha, username (opcional), dispositivo (opcional)
- **REQ-005**: Crear una implementación `ConexionMongoDB` que implemente la interfaz `Conexion`
- **REQ-006**: Permitir al usuario seleccionar qué medio de persistencia usar (Shared Preferences, SQLite o MongoDB)

### Requisitos No Funcionales

- **RNF-001**: La sincronización con MongoDB no debe bloquear la interfaz de usuario (usar corrutinas)
- **RNF-002**: Implementar manejo de errores y reintentos en caso de fallos de conectividad
- **RNF-003**: Mantener retrocompatibilidad con registros existentes en SQLite y Shared Preferences

### Requisitos de Seguridad

- **SEC-001**: Las credenciales de MongoDB (conexión string, API keys) deben almacenarse de forma segura (no hardcodeadas)
- **SEC-002**: Implementar autenticación/validación en las peticiones a MongoDB
- **SEC-003**: Validar datos antes de enviar a MongoDB (inyección de datos)

### Restricciones Técnicas

- **CON-001**: Mantener compatibilidad con API mínima de Android (verificar minSdkVersion actual)
- **CON-002**: No modificar la lógica principal del juego en `MyViewModel`
- **CON-003**: Usar la misma estructura de data class `Record` existente
- **CON-004**: Las operaciones de lectura/escritura deben ser asincrónicas (corrutinas de Kotlin)

### Patrones y Guías

- **PAT-001**: Patrón Strategy: Implementar diferentes estrategias de persistencia intercambiables
- **PAT-002**: Patrón Repository: Encapsular la lógica de acceso a datos
- **PAT-003**: Patrón Adapter: Adaptar la respuesta de MongoDB al formato local
- **GUD-001**: Seguir gitflow: crear rama `feature/mongodb-integration` para el desarrollo
- **GUD-002**: Usar nombres descriptivos para commits: `feat(mongodb): add ConexionMongoDB implementation`
- **GUD-003**: Documentar decisiones técnicas en `DECISION_LOG.md`

---

## 2. Pasos de Implementación

### Fase 1: Investigación e Infraestructura

**OBJETIVO-FASE1**: Evaluar opciones de MongoDB, elegir la mejor, configurar dependencias y preparar la infraestructura necesaria.

| Tarea | Descripción | Completada | Fecha |
|-------|-------------|------------|-------|
| TAREA-101 | **Evaluar opciones MongoDB**: Investigar Firebase Realtime DB, MongoDB Realm, MongoDB Atlas + REST API. Crear documento comparativo en `docs/MONGODB_OPTIONS.md` con pros/contras, latencia estimada, costo, facilidad de integración. Recomendación final justificada. | | |
| TAREA-102 | **Crear archivo de configuración**: Crear archivo `app/src/main/res/values/mongodb_config.xml` (o `local.properties`) para almacenar configuración no sensible. Crear `local.properties.example` con placeholders. | | |
| TAREA-103 | **Agregar dependencias base**: Actualizar `build.gradle.kts` (app) con dependencias iniciales según opción elegida (ej. Firebase BOM, Realm SDK, Retrofit para REST API). | | |
| TAREA-104 | **Crear estructura de paquetes**: Crear directorio `app/src/main/java/com/SarayDani/sidi/mongodb/` con subdirectorios: `model/`, `repository/`, `network/`, `sync/`. | | |
| TAREA-105 | **Documentar decisiones**: Crear archivo `docs/MONGODB_IMPLEMENTATION_DECISION.md` con decisión final, justificación, y arquitectura propuesta. | | |

---

### Fase 2: Modelos y Entidades MongoDB

**OBJETIVO-FASE2**: Crear data classes y entidades necesarias para MongoDB, con serialización/deserialización correcta.

| Tarea | Descripción | Completada | Fecha |
|-------|-------------|------------|-------|
| TAREA-201 | **Crear RecordMongoDB data class**: En `app/src/main/java/com/SarayDani/sidi/mongodb/model/RecordMongoDB.kt`, crear data class con campos: `_id` (String), `puntuacion` (Int), `fecha` (LocalDateTime), `usuario` (String?), `dispositivo` (String?), `sessionId` (String). Agregar anotaciones de serialización (@Serializable o @JsonProperty según framework). | | |
| TAREA-202 | **Crear modelo de sincronización**: En `app/src/main/java/com/SarayDani/sidi/mongodb/model/SyncStatus.kt`, crear enum `SyncStatus` (PENDING, SYNCED, FAILED) y data class `PendingSync` para trackear registros no sincronizados. | | |
| TAREA-203 | **Crear mapper/converter**: En `app/src/main/java/com/SarayDani/sidi/mongodb/model/RecordMapper.kt`, crear funciones extensión para convertir `Record` (local) ↔ `RecordMongoDB` (remoto). | | |
| TAREA-204 | **Extender EntidadRecord**: Actualizar `app/src/main/java/com/SarayDani/sidi/ParaRoom/EntidadRecord.kt` para agregar campos: `mongoId` (String?), `syncStatus` (SyncStatus). Crear migración si Room ya tiene datos. | | |

---

### Fase 3: Capa de Red y Repositorio

**OBJETIVO-FASE3**: Implementar la capa de comunicación con MongoDB y el repositorio centralizado.

| Tarea | Descripción | Completada | Fecha |
|-------|-------------|------------|-------|
| TAREA-301 | **Crear interfaz de servicio MongoDB**: En `app/src/main/java/com/SarayDani/sidi/mongodb/network/MongoDBService.kt`, definir interfaz con métodos: `obtenerRecords()`, `guardarRecord()`, `actualizarRecord()`, `eliminarRecord()`. Usar Retrofit si es REST API o SDK nativo si es Firebase/Realm. | | |
| TAREA-302 | **Implementar cliente HTTP/SDK**: Crear clase `app/src/main/java/com/SarayDani/sidi/mongodb/network/MongoDBClient.kt` para inicializar cliente (Retrofit, Firebase, Realm) con configuración de timeouts, reintentos, y manejo de errores. | | |
| TAREA-303 | **Crear RepositorioMongoDB**: En `app/src/main/java/com/SarayDani/sidi/mongodb/repository/RepositorioMongoDB.kt`, implementar `Conexion` con métodos `obtenerRecord()` y `actualizarRecord()`. Usar corrutinas (suspend functions). Incluir fallback a localStorage si falla conexión. | | |
| TAREA-304 | **Crear RepositorioHibrido**: En `app/src/main/java/com/SarayDani/sidi/mongodb/repository/RepositorioHibrido.kt`, implementar `Conexion` que coordine operaciones entre MongoDB, SQLite y Shared Preferences. Incluir lógica de sincronización. | | |
| TAREA-305 | **Implementar manejo de errores**: Crear clase `app/src/main/java/com/SarayDani/sidi/mongodb/network/NetworkException.kt` y `MongoDBException.kt` para excepciones personalizadas. Implementar retry policy (exponential backoff). | | |

---

### Fase 4: Sincronización Offline-First

**OBJETIVO-FASE4**: Implementar sistema de sincronización que permita offline-first con reintento automático.

| Tarea | Descripción | Completada | Fecha |
|-------|-------------|------------|-------|
| TAREA-401 | **Crear SyncManager**: En `app/src/main/java/com/SarayDani/sidi/mongodb/sync/SyncManager.kt`, crear clase responsable de: detectar cambios locales sin sincronizar, reintentar sincronización cuando hay conexión, manejar conflictos. | | |
| TAREA-402 | **Crear detector de conectividad**: En `app/src/main/java/com/SarayDani/sidi/mongodb/network/ConnectivityManager.kt`, usar `ConnectivityManager` del sistema para monitorear estado de red. Exponer como Flow<Boolean>. | | |
| TAREA-403 | **Implementar cola de sincronización local**: Usar SQLite/Room para almacenar registros pendientes de sincronizar. En `app/src/main/java/com/SarayDani/sidi/ParaRoom/PendingSyncDAO.kt`, crear DAO para gestionar tabla de sincronización pendiente. | | |
| TAREA-404 | **Crear Worker de sincronización**: En `app/src/main/java/com/SarayDani/sidi/mongodb/sync/SyncWorker.kt`, crear `CoroutineWorker` (WorkManager) que se ejecute periódicamente para sincronizar datos pendientes. Configurar con `PeriodicWorkRequest`. | | |
| TAREA-405 | **Implementar merge de datos**: Crear lógica en `SyncManager` para resolver conflictos cuando hay cambios en local y remoto: último en escribir gana, timestamp resolution, o merge custom. Documentar estrategia elegida. | | |

---

### Fase 5: Integración con MyViewModel

**OBJETIVO-FASE5**: Integrar el sistema de persistencia MongoDB en el ViewModel sin romper funcionalidad existente.

| Tarea | Descripción | Completada | Fecha |
|-------|-------------|------------|-------|
| TAREA-501 | **Actualizar inicialización ViewModel**: En `app/src/main/java/com/SarayDani/sidi/MyViewModel.kt`, modificar `init {}` para permitir elegir entre `RepositorioRecord`, `ConexionMongoDB` o `RepositorioHibrido`. Inyectar dependencia via constructor. | | |
| TAREA-502 | **Refactorizar métodos de record**: Actualizar `cargarRecordDesdeRoom()` y métodos relacionados para trabajar con el repositorio inyectado. Mantener compatibilidad con métodos existentes. | | |
| TAREA-503 | **Implementar observables para MongoDB**: Exponer `Flow<Record>` desde ViewModel para cambios en MongoDB. Usar `collect` en composables correspondientes. | | |
| TAREA-504 | **Agregar preferencia de usuario**: En la interfaz (IU.kt o MainActivity), crear opción para que usuario seleccione: "Guardar localmente (SQLite)", "Usar Shared Preferences", o "Sincronizar con MongoDB". Guardar preferencia en SharedPreferences. | | |
| TAREA-505 | **Manejar ciclo de vida de sincronización**: En `MainActivity.kt`, iniciar/detener `SyncWorker` según preferencia y estado de la aplicación. Limpiar recursos en `onDestroy`. | | |

---

### Fase 6: Capa de Persistencia Local para Cola de Sync

**OBJETIVO-FASE6**: Implementar tablas y DAOs en Room para almacenar registros pendientes de sincronizar.

| Tarea | Descripción | Completada | Fecha |
|-------|-------------|------------|-------|
| TAREA-601 | **Crear EntidadPendingSync**: En `app/src/main/java/com/SarayDani/sidi/ParaRoom/EntidadPendingSync.kt`, crear data class con anotaciones Room: `@Entity`, campos: `id` (PK), `recordId`, `operacion` (INSERT/UPDATE/DELETE), `payload` (JSON), `timestamp`, `intentos`. | | |
| TAREA-602 | **Crear PendingSyncDAO**: En `app/src/main/java/com/SarayDani/sidi/ParaRoom/PendingSyncDAO.kt`, implementar métodos: `insertarPending()`, `obtenerPendientes()`, `marcarSincronizado()`, `eliminarPending()`. | | |
| TAREA-603 | **Actualizar BaseDatosSimonDice**: En `app/src/main/java/com/SarayDani/sidi/ParaRoom/Database.kt`, agregar entidad `EntidadPendingSync` y DAO `PendingSyncDAO`. Incrementar `version` de la BD. Crear migration si es necesario. | | |
| TAREA-604 | **Crear RepositorioPendingSync**: En `app/src/main/java/com/SarayDani/sidi/mongodb/repository/RepositorioPendingSync.kt`, wrapper sobre `PendingSyncDAO` para operaciones sobre cola de sincronización. | | |

---

### Fase 7: Interfaz de Usuario y Configuración

**OBJETIVO-FASE7**: Crear UI para configurar MongoDB y mostrar estado de sincronización.

| Tarea | Descripción | Completada | Fecha |
|-------|-------------|------------|-------|
| TAREA-701 | **Crear pantalla de configuración**: Crear composable `app/src/main/java/com/SarayDani/sidi/ui/ConfiguracionPersistencia.kt` con opciones: radio buttons para elegir proveedor (Local/SQLite, Shared Preferences, MongoDB), campo para API key/credenciales (si aplica). | | |
| TAREA-702 | **Crear indicador de sincronización**: Agregar composable `app/src/main/java/com/SarayDani/sidi/ui/SyncStatusIndicator.kt` que muestre estado: "✓ Sincronizado", "⟳ Sincronizando", "⚠ Pendiente", "✗ Error". Hacer clickeable para ver detalles. | | |
| TAREA-703 | **Integrar UI en MainActivity**: En `app/src/main/java/com/SarayDani/sidi/MainActivity.kt`, agregar botones/menú para acceder a configuración y mostrar indicador de sincronización. | | |
| TAREA-704 | **Crear pantalla de estado de sincronización**: Composable `app/src/main/java/com/SarayDani/sidi/ui/SyncStatusDetail.kt` que muestre: registros pendientes, último sync, errores recientes, botón para "Sincronizar ahora". | | |
| TAREA-705 | **Agregar notificaciones**: Implementar notificaciones locales cuando sincronización falla o éxito. Usar Notification API de Android. | | |

---

### Fase 8: Testing

**OBJETIVO-FASE8**: Crear tests unitarios e integrados para validar funcionalidad de MongoDB y sincronización.

| Tarea | Descripción | Completada | Fecha |
|-------|-------------|------------|-------|
| TAREA-801 | **Tests unitarios RepositorioMongoDB**: En `app/src/test/java/com/SarayDani/sidi/mongodb/RepositorioMongoDBTest.kt`, mockear MongoDBService, probar obtener/actualizar record, manejo de excepciones. | | |
| TAREA-802 | **Tests unitarios SyncManager**: En `app/src/test/java/com/SarayDani/sidi/mongodb/SyncManagerTest.kt`, probar sincronización offline-first, merge de datos, reintentos. | | |
| TAREA-803 | **Tests instrumentados RepositorioHibrido**: En `app/src/androidTest/java/com/SarayDani/sidi/mongodb/RepositorioHibridoInstrumentedTest.kt`, probar coordinación entre MongoDB, SQLite y Shared Preferences con conexión/desconexión simulada. | | |
| TAREA-804 | **Tests de conectividad**: En `app/src/androidTest/java/com/SarayDani/sidi/mongodb/ConnectivityTest.kt`, simular cambios de estado de red y verificar comportamiento correcto. | | |
| TAREA-805 | **Tests de mappers**: En `app/src/test/java/com/SarayDani/sidi/mongodb/RecordMapperTest.kt`, probar conversión bidireccional Record ↔ RecordMongoDB. | | |
| TAREA-806 | **Test de migración**: En `app/src/androidTest/java/com/SarayDani/sidi/ParaRoom/MigrationTest.kt`, probar migración Room al agregar nuevos campos. | | |

---

### Fase 9: Documentación y Deployment

**OBJETIVO-FASE9**: Documentar cambios, crear guías de uso, y preparar para release.

| Tarea | Descripción | Completada | Fecha |
|-------|-------------|------------|-------|
| TAREA-901 | **Crear guía de configuración MongoDB**: Documento `docs/MONGODB_SETUP.md` explicando: cómo crear cuenta MongoDB, obtener conexión string, configurar credenciales, testear conexión. | | |
| TAREA-902 | **Documentar arquitectura**: Crear `docs/ARCHITECTURE_MONGODB.md` con diagramas (Mermaid) de flujos de sincronización, capas, componentes. | | |
| TAREA-903 | **Crear CHANGELOG**: En `CHANGELOG.md` ó `RELEASES.md`, documentar: nuevas características, breaking changes, migraciones necesarias. | | |
| TAREA-904 | **Escribir README actualizado**: Actualizar `README.md` con sección "Persistencia de Datos" explicando opciones disponibles (SQLite, SharedPreferences, MongoDB). | | |
| TAREA-905 | **Crear guía de troubleshooting**: Documento `docs/MONGODB_TROUBLESHOOTING.md` con problemas comunes y soluciones: conexión fallida, sincronización lenta, credenciales inválidas. | | |
| TAREA-906 | **Preparar release**: Crear tag `release/1.1.0` (siguiendo SemVer), actualizar `gradle.properties` con versión, crear PR a `release/1.1` desde `feature/mongodb-integration`. | | |

---

## 3. Alternativas

- **ALT-001**: Usar **Firebase Realtime Database** en lugar de MongoDB Atlas
  - **Ventajas**: Integración más sencilla, SDK oficial, no necesita gestionar infraestructura
  - **Desventajas**: Lock-in con Google, costos pueden variar, menos flexible que MongoDB
  - **Por qué no se elige**: Usuario específicamente pidió MongoDB

- **ALT-002**: Usar **GraphQL (Apollo)** en lugar de REST API
  - **Ventajas**: Queries más eficientes, tipado fuerte
  - **Desventajas**: Complejidad adicional, overhead para esta escala
  - **Por qué no se elige**: REST API es suficiente, menor complejidad

- **ALT-003**: Almacenar TODO en MongoDB sin mantener SQLite/Shared Preferences
  - **Ventajas**: Arquitectura más simple, una sola fuente de verdad
  - **Desventajas**: Rompe compatibilidad, requiere migración masiva, difícil rollback
  - **Por qué no se elige**: Requisito explícito es mantener la estructura existente

- **ALT-004**: Sincronización manual vs automática
  - **Ventajas (manual)**: Usuario controla cuándo sincronizar
  - **Desventajas (manual)**: Puede haber desync, experiencia pobre
  - **Elegida (automática)**: mejor UX, sincronización en background con WorkManager

---

## 4. Dependencias

### Dependencias Externas (librerías)

- **DEP-001**: Realm Kotlin SDK ó Firebase SDK (según decisión TAREA-101)
- **DEP-002**: Retrofit 2.x (si se usa REST API)
- **DEP-003**: OkHttp 4.x (para HTTP)
- **DEP-004**: Gson o Kotlinx Serialization (serialización JSON)
- **DEP-005**: WorkManager (para sincronización periódica)
- **DEP-006**: Coroutines (ya presente en proyecto)

### Dependencias de Proyecto

- **DEP-007**: Room/SQLite existente (actualizar para nuevas entidades)
- **DEP-008**: Shared Preferences existente
- **DEP-009**: ViewModel y Lifecycle existente

### Dependencias de Tareas

| Tarea | Depende de |
|-------|-----------|
| TAREA-102 | TAREA-101 |
| TAREA-103 | TAREA-101 |
| TAREA-105 | TAREA-101 |
| TAREA-201 | TAREA-104 |
| TAREA-203 | TAREA-201 |
| TAREA-204 | TAREA-201 |
| TAREA-301 | TAREA-103 |
| TAREA-302 | TAREA-301 |
| TAREA-303 | TAREA-302 |
| TAREA-304 | TAREA-303 |
| TAREA-305 | TAREA-302 |
| TAREA-401 | TAREA-304 |
| TAREA-402 | TAREA-104 |
| TAREA-403 | TAREA-204 |
| TAREA-404 | TAREA-401, TAREA-403 |
| TAREA-405 | TAREA-401 |
| TAREA-501 | TAREA-304 |
| TAREA-502 | TAREA-501 |
| TAREA-503 | TAREA-502 |
| TAREA-504 | TAREA-502 |
| TAREA-505 | TAREA-404 |
| TAREA-601 | TAREA-104 |
| TAREA-602 | TAREA-601 |
| TAREA-603 | TAREA-602 |
| TAREA-604 | TAREA-602 |
| TAREA-701 | TAREA-504 |
| TAREA-702 | TAREA-503 |
| TAREA-703 | TAREA-701, TAREA-702 |
| TAREA-704 | TAREA-703 |
| TAREA-705 | TAREA-704 |
| TAREA-801 | TAREA-303 |
| TAREA-802 | TAREA-401 |
| TAREA-803 | TAREA-304 |
| TAREA-804 | TAREA-402 |
| TAREA-805 | TAREA-203 |
| TAREA-806 | TAREA-603 |
| TAREA-901 | TAREA-303 |
| TAREA-902 | TAREA-304 |
| TAREA-903 | TAREA-906 |
| TAREA-904 | TAREA-303 |
| TAREA-905 | TAREA-902 |
| TAREA-906 | TAREA-904 |

---

## 5. Archivos

### Archivos a Crear

| Archivo | Descripción | Carpeta |
|---------|-------------|---------|
| **ARCHIVO-101** | `docs/MONGODB_OPTIONS.md` | Análisis comparativo de opciones | `docs/` |
| **ARCHIVO-102** | `docs/MONGODB_IMPLEMENTATION_DECISION.md` | Decisión final y justificación | `docs/` |
| **ARCHIVO-103** | `app/src/main/res/values/mongodb_config.xml` | Configuración no sensible | `app/src/main/res/values/` |
| **ARCHIVO-104** | `local.properties.example` | Template para credenciales | Root |
| **ARCHIVO-201** | `app/src/main/java/com/SarayDani/sidi/mongodb/model/RecordMongoDB.kt` | Data class de MongoDB | `mongodb/model/` |
| **ARCHIVO-202** | `app/src/main/java/com/SarayDani/sidi/mongodb/model/SyncStatus.kt` | Enum y modelos de sync | `mongodb/model/` |
| **ARCHIVO-203** | `app/src/main/java/com/SarayDani/sidi/mongodb/model/RecordMapper.kt` | Conversiones Record ↔ RecordMongoDB | `mongodb/model/` |
| **ARCHIVO-301** | `app/src/main/java/com/SarayDani/sidi/mongodb/network/MongoDBService.kt` | Interfaz de servicio | `mongodb/network/` |
| **ARCHIVO-302** | `app/src/main/java/com/SarayDani/sidi/mongodb/network/MongoDBClient.kt` | Cliente HTTP/SDK | `mongodb/network/` |
| **ARCHIVO-303** | `app/src/main/java/com/SarayDani/sidi/mongodb/network/NetworkException.kt` | Excepciones personalizadas | `mongodb/network/` |
| **ARCHIVO-304** | `app/src/main/java/com/SarayDani/sidi/mongodb/repository/RepositorioMongoDB.kt` | Implementación Conexion para MongoDB | `mongodb/repository/` |
| **ARCHIVO-305** | `app/src/main/java/com/SarayDani/sidi/mongodb/repository/RepositorioHibrido.kt` | Coordinador multiplataforma | `mongodb/repository/` |
| **ARCHIVO-401** | `app/src/main/java/com/SarayDani/sidi/mongodb/sync/SyncManager.kt` | Gestor de sincronización | `mongodb/sync/` |
| **ARCHIVO-402** | `app/src/main/java/com/SarayDani/sidi/mongodb/network/ConnectivityManager.kt` | Detector de conectividad | `mongodb/network/` |
| **ARCHIVO-403** | `app/src/main/java/com/SarayDani/sidi/mongodb/sync/SyncWorker.kt` | Worker periódico | `mongodb/sync/` |
| **ARCHIVO-601** | `app/src/main/java/com/SarayDani/sidi/ParaRoom/EntidadPendingSync.kt` | Entidad Room de pending | `ParaRoom/` |
| **ARCHIVO-602** | `app/src/main/java/com/SarayDani/sidi/ParaRoom/PendingSyncDAO.kt` | DAO para pending | `ParaRoom/` |
| **ARCHIVO-701** | `app/src/main/java/com/SarayDani/sidi/ui/ConfiguracionPersistencia.kt` | UI de configuración | `ui/` |
| **ARCHIVO-702** | `app/src/main/java/com/SarayDani/sidi/ui/SyncStatusIndicator.kt` | Indicador de sync | `ui/` |
| **ARCHIVO-703** | `app/src/main/java/com/SarayDani/sidi/ui/SyncStatusDetail.kt` | Detalle de sync | `ui/` |
| **ARCHIVO-801** | `app/src/test/java/com/SarayDani/sidi/mongodb/RepositorioMongoDBTest.kt` | Tests unitarios | `tests/` |
| **ARCHIVO-802** | `app/src/test/java/com/SarayDani/sidi/mongodb/SyncManagerTest.kt` | Tests sync | `tests/` |
| **ARCHIVO-803** | `app/src/androidTest/java/com/SarayDani/sidi/mongodb/RepositorioHibridoInstrumentedTest.kt` | Tests integración | `androidTests/` |
| **ARCHIVO-901** | `docs/MONGODB_SETUP.md` | Guía de configuración | `docs/` |
| **ARCHIVO-902** | `docs/ARCHITECTURE_MONGODB.md` | Diagramas de arquitectura | `docs/` |
| **ARCHIVO-903** | `docs/MONGODB_TROUBLESHOOTING.md` | Troubleshooting | `docs/` |

### Archivos a Modificar

| Archivo | Descripción |
|---------|-------------|
| **ARCHIVO-M01** | `build.gradle.kts` (app) | Agregar dependencias de MongoDB, Retrofit, OkHttp, WorkManager |
| **ARCHIVO-M02** | `app/src/main/AndroidManifest.xml` | Agregar permisos: INTERNET, ACCESS_NETWORK_STATE. Registrar WorkManager |
| **ARCHIVO-M03** | `app/src/main/java/com/SarayDani/sidi/MyViewModel.kt` | Refactorizar init{}, agregar inyección de repositorio |
| **ARCHIVO-M04** | `app/src/main/java/com/SarayDani/sidi/MainActivity.kt` | Integrar UI de configuración, iniciar SyncWorker |
| **ARCHIVO-M05** | `app/src/main/java/com/SarayDani/sidi/ParaRoom/Database.kt` | Agregar EntidadPendingSync, incrementar versión BD |
| **ARCHIVO-M06** | `app/src/main/java/com/SarayDani/sidi/ParaRoom/EntidadRecord.kt` | Agregar campos mongoId, syncStatus |
| **ARCHIVO-M07** | `app/src/main/java/com/SarayDani/sidi/IU.kt` | Integrar indicador de sincronización |
| **ARCHIVO-M08** | `README.md` | Actualizar con sección "Persistencia de Datos" |
| **ARCHIVO-M09** | `gradle.properties` | Actualizar versión a 1.1.0 |

---

## 6. Pruebas

### Pruebas Unitarias

- **PRUEBA-001**: `testRepositorioMongoDBObtenerRecord` - Verifica que `RepositorioMongoDB.obtenerRecord()` devuelve Record correcto
- **PRUEBA-002**: `testRepositorioMongoDBActualizarRecord` - Verifica actualización en MongoDB
- **PRUEBA-003**: `testRepositorioMongoDBManejoErrores` - Verifica manejo de excepciones (conexión fallida, timeout)
- **PRUEBA-004**: `testSyncManagerOfflineFirst` - Verifica que cambios se almacenan localmente sin conexión
- **PRUEBA-005**: `testSyncManagerMergeConflictos` - Verifica resolución de conflictos local vs remoto
- **PRUEBA-006**: `testRecordMapperBidireccional` - Verifica conversiones Record ↔ RecordMongoDB sin pérdida de datos
- **PRUEBA-007**: `testRecordMapperValoresNulos` - Verifica mapeo con campos opcionales nulos

### Pruebas de Integración (Instrumented)

- **PRUEBA-008**: `testRepositorioHibridoCoordinacion` - Verifica sincronización correcta entre MongoDB, SQLite y SharedPreferences
- **PRUEBA-009**: `testSyncWorkerEjecucionPeriodica` - Verifica que WorkManager ejecuta sincronización periódicamente
- **PRUEBA-010**: `testConnectivityDetection` - Verifica detección de cambios de estado de red (conectado/desconectado)
- **PRUEBA-011**: `testSyncConReconexion` - Simula: offline → cambios locales → online → verifica sincronización
- **PRUEBA-012**: `testMigracionRoomNuevosCampos` - Verifica migración DB sin pérdida de datos
- **PRUEBA-013**: `testUIConfiguracionPersistencia` - Verifica selección de proveedor y persistencia de preferencia

### Pruebas de UI/Manual

- **PRUEBA-014**: Seleccionar "MongoDB" en configuración, hacer una jugada, verificar que record se guarde
- **PRUEBA-015**: Con MongoDB seleccionado, desactivar WiFi, jugar, verificar indicador "Pendiente", reactivar WiFi, verificar sincronización
- **PRUEBA-016**: Verificar que indicador de sincronización muestre estados correctos: Sincronizado ✓, Sincronizando ⟳, Pendiente ⚠, Error ✗
- **PRUEBA-017**: Abrir "Detalle de Sincronización", verificar lista de registros pendientes y botón "Sincronizar ahora"
- **PRUEBA-018**: Cambiar entre proveedores (SQLite ↔ MongoDB ↔ SharedPreferences), verificar que se cargan records correspondientes

### Criterios de Aceptación

- Todas las PRUEBA-001 a PRUEBA-007 deben pasar (cobertura >90%)
- Todas las PRUEBA-008 a PRUEBA-012 deben pasar en dispositivo real o emulador
- Todas las PRUEBA-014 a PRUEBA-018 deben ejecutarse sin errores
- No debe haber regresiones en funcionalidad existente del juego
- Sincronización no debe bloquear UI (todos los métodos con `suspend` o en `viewModelScope.launch`)

---

## 7. Riesgos y Suposiciones

### Riesgos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|--------|-----------|
| **RIESGO-001** | Credenciales MongoDB comprometidas | Alta | Baja | Usar `local.properties` (no commitear), considerar subrouting API en backend propio |
| **RIESGO-002** | Latencia de red afecta UX | Media | Media | Implementar UI reactiva, caché local, sincronización async |
| **RIESGO-003** | Conflictos en sincronización (datos inconsistentes) | Media | Alta | Implementar last-write-wins, timestamp, test de merge |
| **RIESGO-004** | Room migration falla (corrupción BD) | Baja | Alta | Testear migrations antes de release, hacer backup automático |
| **RIESGO-005** | WorkManager no se ejecuta en algunos dispositivos (Doze mode) | Alta | Baja | Usar `setExpedited()`, documentar limitaciones, fallback a manual |
| **RIESGO-006** | Cambio en estructura de datos futuro requiere migración compleja | Media | Media | Diseñar modelos extensibles, versionado de API |
| **RIESGO-007** | Dependencia en Firebase/Realm sale del soporte | Baja | Alta | Abstractizar con interfaz `Conexion`, permite cambio futuro |

### Suposiciones

- **SUP-001**: Existe conexión a Internet disponible (puede ser intermitente, manejado con offline-first)
- **SUP-002**: API de MongoDB (o proveedor elegido) es estable y tiene 99.9% uptime
- **SUP-003**: minSdkVersion del proyecto es ≥ 24 (necesario para algunas APIs)
- **SUP-004**: Usuario tiene intención de compartir datos entre dispositivos o hacer backup en MongoDB
- **SUP-005**: Team tiene acceso a cuenta MongoDB/Firebase para configuración
- **SUP-006**: Struktura actual de `Conexion` es correcta y no cambiará
- **SUP-007**: Tests existentes seguirán pasando tras cambios en persistencia

---

## 8. Especificaciones Relacionadas / Para Saber Más

- [FEATURE_MONGODB.md](./FEATURE_MONGODB.md) - Documento original de requisitos
- [README.md](./README.md) - Documentación actual de la aplicación
- [Android Documentation: WorkManager](https://developer.android.com/guide/background#use-cases)
- [Android Documentation: Room Database](https://developer.android.com/training/data-storage/room)
- [Android Documentation: Connectivity Manager](https://developer.android.com/guide/topics/connectivity)
- [Kotlin Coroutines Documentation](https://kotlinlang.org/docs/coroutines-overview.html)
- [MongoDB Realm Kotlin SDK Documentation](https://www.mongodb.com/docs/realm/sdk/kotlin/)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [SemVer](https://semver.org/lang/es/)
- [Gitflow Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)

---

## Matriz de Trazabilidad

### Requisitos → Tareas

| Requisito | Tareas Relacionadas |
|-----------|-------------------|
| REQ-001 | TAREA-304, TAREA-501, TAREA-503 |
| REQ-002 | TAREA-401, TAREA-402, TAREA-404 |
| REQ-003 | TAREA-301, TAREA-303 |
| REQ-004 | TAREA-201 |
| REQ-005 | TAREA-303 |
| REQ-006 | TAREA-504, TAREA-701 |
| SEC-001 | TAREA-102, TAREA-302 |
| SEC-002 | TAREA-302 |
| SEC-003 | TAREA-305 |

### Tareas → Pruebas

| Tarea | Pruebas Relacionadas |
|-------|-------------------|
| TAREA-303 | PRUEBA-001, PRUEBA-002, PRUEBA-003 |
| TAREA-401 | PRUEBA-004, PRUEBA-005 |
| TAREA-203 | PRUEBA-006, PRUEBA-007 |
| TAREA-304 | PRUEBA-008 |
| TAREA-404 | PRUEBA-009 |
| TAREA-402 | PRUEBA-010, PRUEBA-011 |
| TAREA-603 | PRUEBA-012 |
| TAREA-701 | PRUEBA-013, PRUEBA-014 |
| TAREA-702 | PRUEBA-016, PRUEBA-017 |
| TAREA-504 | PRUEBA-018 |

---

## Flujo de Trabajo Recomendado (GitFlow)

```
main (release)
  ↑
release/1.1
  ↑
feature/mongodb-integration (este plan)
  ├── Fase 1-2: investigación + modelos
  ├── Fase 3-4: red + sync
  ├── Fase 5-6: integración + persistencia
  ├── Fase 7-9: UI + tests + docs
  ↓
develop (continuo)
```

**Comandos Git:**
```bash
git checkout develop
git pull origin develop
git checkout -b feature/mongodb-integration
# Trabajo en ramas por fase si es necesario:
# git checkout -b feature/mongodb-models (desde develop)
# Commits con prefijo: feat(mongodb): ...
# Al finalizar: git push origin feature/mongodb-integration
# Crear PR en GitHub para revisión
```

---

## Estado de Implementación

| Fase | Estado | % Completado | Próximo Paso |
|------|--------|--------------|------------|
| Fase 1 | Planificado | 0% | Ejecutar TAREA-101 |
| Fase 2 | Planificado | 0% | Esperar Fase 1 |
| Fase 3 | Planificado | 0% | Esperar Fase 1 |
| Fase 4 | Planificado | 0% | Esperar Fase 3 |
| Fase 5 | Planificado | 0% | Esperar Fase 4 |
| Fase 6 | Planificado | 0% | Esperar Fase 4 |
| Fase 7 | Planificado | 0% | Esperar Fase 5 |
| Fase 8 | Planificado | 0% | Esperar Fase 7 |
| Fase 9 | Planificado | 0% | Esperar Fase 8 |

---

**Fecha de Próxima Revisión:** 2026-02-02  


