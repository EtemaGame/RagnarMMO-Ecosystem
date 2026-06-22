# RagnarMMO Inventory Direction

Estado: diferido hasta definir el menu personalizado.

## Decision actual

No se va a extender visualmente el inventario vanilla con slots sueltos.

El sistema final debe usar menus propios de RagnarMMO que intercepten o reemplacen el flujo del `InventoryMenu` vanilla cuando corresponda. El inventario, equipment extra, quick move, validacion y stats deben resolverse como un sistema nuevo y coherente.

## Alcance por ahora

- Mantener solo la logica interna que no bloquee el resto del mod.
- No invertir tiempo en UI final de inventario hasta tener el diseno de menus.
- No asumir que los slots actuales (`MID_HEAD`, `ACCESSORY_1`, `ACCESSORY_2`) seran definitivos.
- No construir mixins de inventario todavia.
- No depender de assets, iconos o pantallas temporales para validar formulas.

## Reglas para el rediseno

- El servidor decide validez, persistencia y efectos de equipo.
- El cliente solo muestra estado y envia acciones.
- Stats y derived stats deben recalcularse desde el estado real equipado.
- Quick move y transferencias deben respetar reglas server-side.
- No copiar slots vanilla como almacenamiento de equipo RagnarMMO.
- No crear dependencias circulares entre `items`, `core`, `combat` y `player`.

## Pendiente

1. Definir el menu principal de personaje/inventario.
2. Definir slots finales de equipment.
3. Definir comandos temporales de inspeccion/debug.
4. Reescribir capability/equipment si el modelo final lo requiere.
5. Integrar stats cuando el almacenamiento final este decidido.
