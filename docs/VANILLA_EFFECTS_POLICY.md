# Vanilla Effects Policy

Estado al 2026-06-22. Este documento define como tratar efectos vanilla de Minecraft dentro de RagnarMMO para evitar incongruencias con stats, combate y skills estilo Ragnarok Online.

## Decision

No se eliminan todos los efectos vanilla de golpe.

Se usan como puente de compatibilidad cuando Minecraft u otros mods los aplican, pero la logica principal de RagnarMMO debe vivir en estados propios o formulas propias.

Regla:

- Efecto visual/feedback: permitido si no altera formulas centrales.
- Efecto vanilla con impacto mecanico: debe traducirse a una semantica RagnarMMO.
- Skill RO nueva: no debe depender de un `MobEffects` vanilla como su formula real.

## Categorias

### Compatibilidad aceptada

Estos efectos pueden existir porque Minecraft los usa de forma natural o porque otros sistemas los pueden aplicar:

- `POISON`
- `WITHER`
- `BLINDNESS`
- `CONFUSION`
- `INVISIBILITY`
- `GLOWING`
- `MOVEMENT_SLOWDOWN`
- `MOVEMENT_SPEED`

Pero no deben ser la fuente final de verdad si afectan formulas RO.

### Logica RO propia

Estos casos deben manejarse con tags/servicios propios, como `RoCombatStatusService`:

- STR/AGI/VIT/INT/DEX/LUK temporales.
- Hard DEF y soft DEF temporales.
- HIT/FLEE/ASPD/cast derivados.
- Provoke.
- Endure.
- Blessing.
- Angelus.
- Signum Crucis.
- Increase/Decrease AGI.
- Magnum Break fire bonus.

### Efectos vanilla peligrosos

Estos efectos no deben usarse como formula principal de skills RO:

- `DAMAGE_RESISTANCE`
- `DAMAGE_BOOST`
- `WEAKNESS`
- `DIG_SPEED`
- `REGENERATION`
- `ABSORPTION`
- `FIRE_RESISTANCE`

Si aparecen, deben ser migrados a un estado RO o quedar solo como feedback temporal documentado.

## Cure / Detoxify

Pre-Renewal estricto:

- Cure remueve Blind, Chaos/Confusion y Silence.
- Detoxify remueve Poison.

Decision practica actual:

- Mientras `POISON` y `WITHER` sean los equivalentes vanilla usados para veneno/dano degenerativo, Cure tambien los limpia para evitar incongruencias jugables.
- Cuando exista un sistema propio de estados RO, Poison deberia moverse a Detoxify y Cure volver a limpiar solo su grupo RO.

Estado actual:

- Cure limpia `POISON`, `WITHER`, `BLINDNESS` y `CONFUSION`.
- Silence y Chaos propios quedan pendientes.

## Ruwach / Hiding

Estado actual:

- `INVISIBILITY` se usa como equivalente temporal de Hiding/Cloaking.
- Ruwach remueve `INVISIBILITY`, aplica `GLOWING` como feedback y dispara dano Holy cuando revela.

Pendiente:

- Crear estado propio de Hiding/Cloaking.
- Ruwach debe depender de ese estado propio, no de invisibilidad vanilla.

## Velocidad

Estado actual:

- Increase AGI y Decrease AGI ya tienen estado RO propio para AGI.
- Se aplica `MOVEMENT_SPEED` o `MOVEMENT_SLOWDOWN` solo como puente visible de movimiento.

Pendiente:

- Reemplazar los cambios de movimiento por un modificador controlado por RagnarMMO si se necesita precision completa.

## Dano degenerativo

Problema:

- `POISON` y `WITHER` hacen dano vanilla fuera del pipeline de combate RO.

Decision:

- A corto plazo se aceptan como compatibilidad.
- A medio plazo deben interceptarse y convertirse a tick damage RO, con formula propia, elemento/propiedad si corresponde, y reglas de reduccion definidas.

## Trabajo pendiente

- Crear `RoStatusType` o equivalente para estados RO canonicos.
- Crear puente vanilla -> RO para efectos comunes.
- Decidir por skill si aplica estado RO, feedback visual o ambos.
- Interceptar dano de `POISON`/`WITHER` si queremos que pase por formulas propias.
- Revisar todas las skills que todavia usan `DAMAGE_RESISTANCE`, `WEAKNESS`, `MOVEMENT_SLOWDOWN`, `INVISIBILITY` o `GLOWING`.
