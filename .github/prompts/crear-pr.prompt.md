# Crear Pull Request en GitHub usando Especificaciones

Crear una Pull Request en GitHub automáticamente, siguiendo las instrucciones escritas en un archivo de especificaciones.

## ¿Qué harás? (Proceso paso a paso)

1. **Leer las instrucciones**: Primero, buscarás y leerás el archivo de especificaciones (`.github/pull_request_template.md`) para entender qué hay que hacer.

2. **Crear el borrador**: Usarás la herramienta 'create_pull_request' para crear un borrador de Pull Request. Pero antes, verificarás si ya existe una Pull Request para esa rama usando 'get_pull_request'. Si ya existe, pasas al paso 4.

3. **Ver los cambios**: Si creaste una nueva Pull Request, usarás 'get_pull_request_diff' para ver exactamente qué archivos se modificaron.

4. **Completar la información**: Con la información del archivo de especificaciones (paso 1), actualizarás el título y la descripción de la Pull Request usando 'update_pull_request'.

5. **Marcar como lista**: Cambiarás el estado de la Pull Request de "borrador" a "lista para revisión" usando 'update_pull_request'.

6. **Asignar al autor**: Obtendrás tu nombre de usuario con 'get_me' y te asignarás a ti mismo como responsable de la Pull Request usando 'update_issue'.

7. **Compartir el enlace**: Finalmente, responderás con la URL de la Pull Request creada para que el usuario pueda verla.

## Reglas importantes (Requisitos)

- **Solo una Pull Request**: Crearás una sola Pull Request que incluya todos los cambios de la especificación.
- **Título claro**: El título debe describir fácilmente qué hace el cambio.
- **Descripción completa**: Debes completar toda la información que pide la plantilla del archivo de especificaciones.
- **Evitar duplicados**: Siempre verificarás si ya existe una Pull Request similar antes de crear una nueva.
