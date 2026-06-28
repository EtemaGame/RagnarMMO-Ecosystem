# Character Select

Estado: implementado como v1 bloqueante.

## Regla activa

- Al conectar, el servidor fuerza un selector de personaje.
- Cada cuenta/player tiene 3 slots.
- Si no existen slots pero el player ya tenia datos previos, se migra una vez a slot 1 usando el estado actual.
- Crear personaje pide nombre y crea siempre `Novice` Lv1.
- Seleccionar carga el estado completo guardado del personaje.
- Volver al selector se hace desde el Pause Menu con `Character Select`.
- Borrar exige escribir el nombre exacto del personaje.

## Datos separados por personaje

- Stats/progresion RO.
- Skills, cooldowns persistidos y hotbar de skills.
- Lifeskills.
- Equipment custom de RagnarMMO.
- Inventario vanilla y ender chest.
- Dimension, posicion, rotacion, vida, food y saturation.

## Bloqueo

Mientras no hay personaje seleccionado, el servidor bloquea movimiento, ataques, skills e interacciones basicas. La pantalla no se puede cerrar con ESC si la seleccion es requerida.

## Pendiente futuro

- Preview visual 3D del personaje.
- Seleccion real de clase inicial cuando se decida permitir primeras clases al crear.
- Integracion con el inventario/menu RO final.
