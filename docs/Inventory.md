# Tarea: extender el inventario vanilla con slots de equipo RagnarMMO

## Objetivo

Modificar el inventario del jugador para agregar nuevos slots reales de equipamiento de RagnarMMO dentro de la pantalla vanilla de inventario.

No crear una pantalla completamente separada como menú alternativo. La intención es que al abrir el inventario normal del jugador se vean slots adicionales para equipamiento RagnarMMO.

Estos slots no son cosméticos. Deben afectar stats, derived stats y reglas de equipamiento.

## Stack técnico

* Minecraft: 1.20.1
* Forge: 47.x
* Proyecto: RagnarMMO-Ecosystem
* No asumir Fabric APIs.
* No copiar código antiguo de Fabric literalmente.
* Se puede usar Mixin si el proyecto ya lo permite o si es necesario configurarlo correctamente.

## Slots iniciales requeridos

Agregar solo estos 3 slots extra en esta primera fase:

* `MID_HEAD`
* `ACCESSORY_1`
* `ACCESSORY_2`

No reemplazar ni reutilizar armor vanilla.

Mantener intactos:

* Helmet vanilla
* Chestplate vanilla
* Leggings vanilla
* Boots vanilla
* Offhand vanilla
* Main hand vanilla
* Inventario principal
* Hotbar

## Regla principal

No copiar slots vanilla.

No hacer esto:

```java
new Slot(playerInventory, sameArmorIndex, x, y)
```

Los slots nuevos deben tener almacenamiento propio.

El diseño correcto es:

```txt
Player
 ├─ Inventory vanilla
 │   ├─ armor
 │   ├─ offhand
 │   ├─ main inventory
 │   └─ hotbar
 │
 └─ RagnarEquipment capability
     ├─ MID_HEAD
     ├─ ACCESSORY_1
     └─ ACCESSORY_2
```

## Arquitectura esperada

Implementar:

1. Capability server-side para los 3 slots extra.
2. `ItemStackHandler` para almacenar los items.
3. Validación server-side de qué item puede ir en cada slot.
4. Persistencia NBT.
5. Sync server → client.
6. Mixin o integración equivalente para agregar slots al `InventoryMenu`.
7. Mixin o integración equivalente para dibujar los slots en `InventoryScreen`.
8. Soporte de shift-click / quick move.
9. Recalculo de derived stats cuando cambia el equipo.
10. Traducciones para nombres/tooltips.

## Ubicación modular sugerida

Usar `ragnarmmo-items` para:

* Equipment slots
* Capability de equipo
* ItemStackHandler
* Reglas de equipamiento
* Slots del menú
* Integración con items/equipment modifiers

Usar `ragnarmmo-core` solo si hace falta exponer o consumir APIs de stats/progression ya existentes.

No introducir dependencias circulares entre módulos.

## Modelo de slots

Crear un enum parecido a:

```java
public enum RagnarEquipmentSlotType {
    MID_HEAD,
    ACCESSORY_1,
    ACCESSORY_2
}
```

Crear constantes de índice:

```java
public final class RagnarEquipmentSlots {
    public static final int MID_HEAD = 0;
    public static final int ACCESSORY_1 = 1;
    public static final int ACCESSORY_2 = 2;
    public static final int SLOT_COUNT = 3;
}
```

## Storage

Crear un `ItemStackHandler` server-side:

```java
public final class RagnarEquipmentHandler extends ItemStackHandler {
    private final Player player;

    public RagnarEquipmentHandler(Player player) {
        super(RagnarEquipmentSlots.SLOT_COUNT);
        this.player = player;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        return switch (slot) {
            case RagnarEquipmentSlots.MID_HEAD ->
                    RagnarEquipmentRules.isMidHead(stack);

            case RagnarEquipmentSlots.ACCESSORY_1,
                 RagnarEquipmentSlots.ACCESSORY_2 ->
                    RagnarEquipmentRules.isAccessory(stack);

            default -> false;
        };
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Recalcular derived stats.
            // Sincronizar equipo al cliente.
        }
    }
}
```

No guardar los items solo en cliente.

## Capability

Implementar capability en el jugador.

Debe cubrir:

* `AttachCapabilitiesEvent<Entity>`
* `PlayerEvent.Clone`
* Login
* Respawn
* Cambio de dimensión
* Sync cuando cambia un slot
* Sync al abrir inventario si es necesario

La capability debe serializar/deserializar el `ItemStackHandler`.

Criterio mínimo:

```txt
equipar item -> cerrar mundo -> abrir mundo -> item sigue en el slot
```

## Slot custom

Crear un slot custom basado en `SlotItemHandler`:

```java
public final class RagnarEquipmentSlot extends SlotItemHandler {
    private final RagnarEquipmentSlotType type;

    public RagnarEquipmentSlot(
            IItemHandler itemHandler,
            int index,
            int x,
            int y,
            RagnarEquipmentSlotType type
    ) {
        super(itemHandler, index, x, y);
        this.type = type;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        return switch (type) {
            case MID_HEAD -> RagnarEquipmentRules.isMidHead(stack);
            case ACCESSORY_1, ACCESSORY_2 -> RagnarEquipmentRules.isAccessory(stack);
        };
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public RagnarEquipmentSlotType getRagnarSlotType() {
        return type;
    }
}
```

La validación final siempre debe ocurrir server-side.

## Integración con InventoryMenu

Agregar los 3 slots extra al `InventoryMenu`.

No insertar slots en medio de los slots vanilla. Agregarlos al final para no romper índices vanilla.

En vanilla 1.20.1, los slots del inventario del jugador normalmente llegan hasta el offhand. Los nuevos slots deberían quedar después del offhand.

Ejemplo conceptual:

```txt
0..45  = slots vanilla del InventoryMenu
46     = MID_HEAD
47     = ACCESSORY_1
48     = ACCESSORY_2
```

Verificar los índices reales en el código antes de hardcodear.

Implementar mediante Mixin sobre:

```txt
net.minecraft.world.inventory.InventoryMenu
```

Inyectar al final del constructor y agregar:

```java
this.addSlot(new RagnarEquipmentSlot(handler, 0, x, y, MID_HEAD));
this.addSlot(new RagnarEquipmentSlot(handler, 1, x, y, ACCESSORY_1));
this.addSlot(new RagnarEquipmentSlot(handler, 2, x, y, ACCESSORY_2));
```

Las coordenadas pueden ajustarse después en la pantalla. Primero debe funcionar la lógica.

## quickMoveStack / shift-click

Modificar el comportamiento de `quickMoveStack`.

Casos obligatorios:

1. Shift-click desde `MID_HEAD`, `ACCESSORY_1` o `ACCESSORY_2` hacia inventario vanilla.
2. Shift-click desde inventario vanilla hacia `MID_HEAD` si el item es válido para mid head.
3. Shift-click desde inventario vanilla hacia `ACCESSORY_1` o `ACCESSORY_2` si el item es válido como accesorio.
4. No mover items inválidos a slots Ragnar.
5. No perder items.
6. No crear ghost items.
7. No duplicar items.

Usar `moveItemStackTo` de `AbstractContainerMenu`. Si es `protected`, crear un Mixin Invoker.

No dejar el `quickMoveStack` vanilla sin parchear para los slots nuevos.

## Integración con InventoryScreen

Modificar visualmente:

```txt
net.minecraft.client.gui.screens.inventory.InventoryScreen
```

Dibujar fondos para los 3 slots extra.

Agregar tooltips traducibles para slots vacíos:

* `slot.ragnarmmo.mid_head`
* `slot.ragnarmmo.accessory_1`
* `slot.ragnarmmo.accessory_2`

Ejemplo de lang:

```json
{
  "slot.ragnarmmo.mid_head": "Mid Head",
  "slot.ragnarmmo.accessory_1": "Accessory 1",
  "slot.ragnarmmo.accessory_2": "Accessory 2"
}
```

No hardcodear textos visibles en la screen.

## Derived stats

Como estos slots no son cosméticos, deben contribuir a stats.

Actualizar el pipeline de derived stats para leer:

```txt
vanilla armor
main hand
offhand
Ragnar equipment capability
```

El recalculo debe ocurrir server-side cuando:

* Se equipa un item Ragnar.
* Se desequipa un item Ragnar.
* Cambia el contenido de la capability.
* El jugador entra al mundo.
* El jugador respawnea.
* El jugador cambia dimensión si el sistema actual requiere resync.

No calcular derived stats definitivos en client-side.

## Reglas de equipamiento

Crear una clase centralizada:

```java
public final class RagnarEquipmentRules {
    public static boolean isMidHead(ItemStack stack) {
        // Validar por tag, interface, componente, registry o sistema existente.
    }

    public static boolean isAccessory(ItemStack stack) {
        // Validar por tag, interface, componente, registry o sistema existente.
    }
}
```

Preferir tags de item para la primera versión:

```txt
ragnarmmo:mid_head_equippable
ragnarmmo:accessory_equippable
```

No hardcodear una lista fija de items en la screen.

## Seguridad server-side

El servidor debe ser autoridad.

El cliente puede mostrar slots y tooltips, pero no decidir:

* si un item es equipable;
* si un item aporta stats;
* si el jugador cumple requisitos;
* si un cambio de equipo es válido;
* cuánto ATK/MATK/DEF/etc. gana.

Todo eso debe validarse server-side.

## No hacer

No hacer:

* No reemplazar `HEAD`, `CHEST`, `LEGS` vanilla por accesorios.
* No guardar accesorios dentro de índices vanilla de armor.
* No copiar slots vanilla existentes.
* No crear dos slots apuntando al mismo índice de inventario.
* No meter lógica de stats en `InventoryScreen`.
* No implementar los slots solo visualmente.
* No ignorar `quickMoveStack`.
* No romper offhand.
* No romper armor vanilla.
* No crear un menú completamente separado si la tarea es extender la experiencia del inventario vanilla.
* No mezclar estos slots con cosmetics todavía.

## Implementación por fases

### Fase 1 — Storage

Implementar:

* enum de slots;
* `ItemStackHandler`;
* capability;
* persistencia NBT;
* sync básico.

Validación:

```txt
El jugador puede guardar items en los 3 slots desde código/debug y persisten tras relog.
```

### Fase 2 — Slots en InventoryMenu

Implementar:

* Mixin sobre `InventoryMenu`;
* agregar 3 slots al final;
* validar que cliente y servidor tienen el mismo número de slots.

Validación:

```txt
Abrir inventario no crashea.
Los slots existen.
Se pueden colocar items válidos.
Items inválidos son rechazados.
```

### Fase 3 — UI en InventoryScreen

Implementar:

* fondo visual de slots;
* posiciones correctas;
* tooltips traducibles;
* nombres de slots si aplica.

Validación:

```txt
Los slots se ven en el inventario normal.
No tapan elementos vanilla importantes.
Funcionan en GUI scale diferente.
```

### Fase 4 — Shift-click

Implementar:

* quick move desde slots Ragnar al inventario;
* quick move desde inventario a slots Ragnar;
* manejo de accesorios en ambos slots.

Validación:

```txt
Shift-click no duplica items.
Shift-click no borra items.
Shift-click respeta reglas de equipamiento.
```

### Fase 5 — Stats

Implementar:

* lectura de estos slots en el sistema de derived stats;
* recalculo server-side al cambiar equipo;
* sync de stats al cliente.

Validación:

```txt
Equipar accesorio cambia derived stats.
Desequipar accesorio revierte derived stats.
El cambio persiste tras cerrar/reabrir inventario.
```

## Criterios de aceptación

La tarea está terminada cuando:

1. El inventario vanilla abre sin crash.
2. Se muestran `MID_HEAD`, `ACCESSORY_1` y `ACCESSORY_2`.
3. Los slots tienen almacenamiento propio.
4. Los items persisten tras relog.
5. Los slots rechazan items inválidos.
6. Shift-click funciona en ambas direcciones.
7. No se rompe armor vanilla.
8. No se rompe offhand.
9. No hay duplicación ni pérdida de items.
10. Los nuevos slots afectan derived stats.
11. El servidor valida todo.
12. El cliente solo muestra datos e input.
13. El código respeta la modularidad de RagnarMMO.
14. El build pasa.
15. `runClient` y dedicated server funcionan sin crash.
