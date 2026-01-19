- Cuando guardas cambios en Git, escribe mensajes claros que expliquen **qué hiciste** y **por qué**. Esto te ayudará a ti y a tu equipo a entender el historial del proyecto.

### Formato básico

Sigue esta estructura simple:

```
Título corto (máximo 50 caracteres)

Descripción más detallada que explica:
- Qué cambiaste exactamente
- Por qué era necesario
- Si hay algo importante que saber
```

### Ejemplos buenos ✅

**Ejemplo 1: Arreglar un error**
```
Corregir cálculo de precios en carrito

El cálculo no incluía el IVA en productos en oferta.
Ahora se aplica correctamente el 21% de IVA a todos
los productos, incluidas las ofertas.
```

**Ejemplo 2: Añadir nueva función**
```
Añadir login con Google

- Implementar botón de "Iniciar con Google"
- Guardar información básica del usuario
- Redirigir al dashboard después del login

Esto permite a los usuarios registrarse más rápido.
```

**Ejemplo 3: Mejorar código existente**
```
Refactorizar función de validación de email

- Separar la lógica en funciones más pequeñas
- Añadir pruebas unitarias
- Mejorar mensajes de error

La función original era difícil de mantener y testear.
```

### Ejemplos malos ❌

```
arreglado
```
→ **¿Qué arreglaste?** No se entiende.

```
cambios
```
→ **¿Qué cambios?** Demasiado vago.

```
commit del martes
```
→ **No explica nada útil** sobre el contenido.

### Consejos importantes

1. **Usa verbos en presente**: "Añadir", "Corregir", "Mejorar" (no "Añadí", "Corregí")
2. **Sé específico**: En lugar de "arreglar bug", escribe "corregir error al cargar imágenes grandes"
3. **Explica el porqué**: ¿Por qué era necesario este cambio?
4. **Menciona archivos clave**: Si cambiaste archivos importantes, menciónalos en la descripción
5. **Usa español claro**: Escribe como hablarías con un compañero

### Cuándo usar commits pequeños

Es mejor hacer varios commits pequeños que uno enorme:
- **Un cambio** = **un commit**
- Si arreglaste un bug y añadiste una función nueva → haz 2 commits separados
- Esto hace más fácil entender los cambios y deshacerlos si es necesario

### Plantilla rápida

```
[Tipo]: Breve descripción

- Qué cambió exactamente
- Por qué se hizo este cambio
- Información adicional si es necesaria

Ejemplo:
Corrección: Solucionar error en cálculo de totales

- La función sum() no incluía los descuentos
- Ahora se aplica el descuento antes de calcular el total
- Esto afecta a las órdenes creadas esta semana
```
