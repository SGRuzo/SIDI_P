# Guía para escribir scripts de Shell (Bash, Zsh, etc.)

Instrucciones para crear scripts que sean claros, seguros y fáciles de mantener.

## Lo más importante

- Escribe código simple y fácil de leer
- Agrega comentarios cuando ayuden a entender qué hace el script
- Usa mensajes breves para mostrar el progreso, sin exagerar
- Revisa tus scripts con `shellcheck` si está disponible
- Prefiere expansiones seguras: usa comillas con las variables (`"$var"`)

## Para evitar errores

- Siempre incluye `set -euo pipefail` al inicio (detiene el script si hay errores)
- Verifica que todos los parámetros necesarios estén presentes antes de empezar
- Muestra mensajes de error claros que expliquen qué salió mal
- Usa `trap` para limpiar archivos temporales si el script termina inesperadamente
- Usa `readonly` para valores que no deben cambiar
- Crea archivos temporales con `mktemp` (más seguro)

## Cómo estructurar un script

- Empieza con `#!/bin/bash` (a menos que necesites otra shell)
- Explica qué hace el script en un comentario al inicio
- Define los valores por defecto de las variables al principio
- Usa funciones para código que se repite
- Mantén la parte principal del script ordenada y fácil de seguir

## Cuando trabajes con JSON o YAML

- Prefiere usar `jq` para JSON y `yq` para YAML (en lugar de `grep` o `awk`)
- Si no tienes `jq`/`yq`, usa la mejor herramienta disponible en tu sistema
- Verifica que los campos requeridos existan antes de usarlos
- Trata los errores de análisis como graves (detén el script si hay problemas)
- Documenta qué herramientas necesitas al inicio del script

## Ejemplo de estructura básica

```bash
#!/bin/bash

# Este script hace [explica brevemente qué hace aquí]

# Detiene el script si hay errores
set -euo pipefail

# Función para limpiar archivos temporales
limpiar() {
    if [[ -n "${DIR_TEMPORAL:-}" && -d "$DIR_TEMPORAL" ]]; then
        rm -rf "$DIR_TEMPORAL"
    fi
}

# Ejecuta 'limpiar' cuando termine el script
trap limpiar EXIT

# Valores por defecto
GRUPO_RECURSOS=""
PARAM_REQUERIDO=""
PARAM_OPCIONAL="valor-por-defecto"
readonly NOMBRE_SCRIPT="$(basename "$0")"

DIR_TEMPORAL=""

# Función que muestra cómo usar el script
mostrar_ayuda() {
    echo "Cómo usar: $NOMBRE_SCRIPT [OPCIONES]"
    echo "Opciones:"
    echo "  -g, --grupo-recursos   Grupo de recursos (requerido)"
    echo "  -h, --help             Muestra esta ayuda"
    exit 0
}

# Función que verifica que esté listo
verificar_requisitos() {
    if [[ -z "$GRUPO_RECURSOS" ]]; then
        echo "Error: Se necesita un grupo de recursos"
        exit 1
    fi
}

# Función principal con la lógica del script
principal() {
    verificar_requisitos

    # Crear directorio temporal
    DIR_TEMPORAL="$(mktemp -d)"
    if [[ ! -d "$DIR_TEMPORAL" ]]; then
        echo "Error: No se pudo crear directorio temporal" >&2
        exit 1
    fi
    
    echo "El script ha comenzado"
    
    # Aquí va la lógica principal del script
    
    echo "El script ha terminado"
}

# Procesar los argumentos (opciones) que te pasan al script
while [[ $# -gt 0 ]]; do
    case $1 in
        -g|--grupo-recursos)
            GRUPO_RECURSOS="$2"
            shift 2
            ;;
        -h|--help)
            mostrar_ayuda
            ;;
        *)
            echo "Opción desconocida: $1"
            exit 1
            ;;
    esac
done

# Ejecutar la función principal
principal "$@"
```

## Consejos adicionales

1. **Nombres claros**: Usa nombres de variables que digan qué son
2. **Comenta lo necesario**: Explica partes complicadas, pero no lo obvio
3. **Prueba tu script**: Ejecútalo en un entorno seguro primero
4. **Manténlo simple**: Si se vuelve muy complicado, considera usar otro lenguaje
